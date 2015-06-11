(set-env!
  :resource-paths #{"resources"}
  :dependencies '[[adzerk/bootlaces          "0.1.11" :scope "test"]
                  [cljsjs/boot-cljsjs        "0.4.8"  :scope "test"]

                  ;; [cljsjs/react              "0.13.3-0"]
                  ;; [cljsjs/object-assign-shim "0.1.0-1"]
                  ])

(require '[adzerk.bootlaces :refer :all]
         '[cljsjs.boot-cljsjs.packaging :refer :all]
         '[clojure.java.io :as io])

(def +version+ "1.4.2-0")
(bootlaces! +version+)

(task-options!
 pom  {:project     'cljsjs/responsive-fixed-data-table
       :version     +version+
       :description "A Responsive wrapped around fixed-data-table."
       :url         "https://github.com/vaiRk/responsive-fixed-data-table"
       :scm         {:url "https://github.com/cljsjs/packages"}
       :license     {"BSD" "http://opensource.org/licenses/BSD-3-Clause"}})

(deftask download-fixed-data-table []
  (download :url      "https://github.com/vaiRk/responsive-fixed-data-table/archive/v1.4.2.zip"
;;            :checksum "d0b0368f02018333d366271535a5d8bf"
            :unzip    true))

(deftask copy-file
  [i in  INPUT  str "Path to file to be copied"
   o out OUTPUT str "Path to where copied file should be saved"]
  (assert in "Path to input file required")
  (assert out "Path to output file required")
  (let [tmp      (temp-dir!)
        out-file (io/file tmp out)]
    (with-pre-wrap fileset
      (let [in-files (input-files fileset)
            in-file  (tmpfile (first (by-re [(re-pattern (str in))] in-files)))]
        (io/make-parents out-file)
        (io/copy in-file out-file)
        (-> fileset
            (add-resource tmp)
            commit!)))))

(deftask package []
  (comp
   (download-fixed-data-table)
   (sift :move {#"^responsive-fixed-data-table-.*/src/responsive-fixed-data-table.js"
                "cljsjs/development/responsive-fixed-data-table.inc.js"})
   (sift :include #{#"^cljsjs"})
   (copy-file :in "^cljsjs/development/responsive-fixed-data-table.inc.js"
              :out "cljsjs/production/responsive-fixed-data-table.inc.js")
   ;; Can't minify, it barfs :-(
   ;; (minify    :in       "cljsjs/development/responsive-fixed-data-table.inc.js"
   ;;            :out      "cljsjs/production/responsive-fixed-data-table.inc.js")
   (sift :include #{#"^cljsjs"})
   (deps-cljs :name "cljsjs.responsive-fixed-data-table"
              :requires ["cljsjs.react" ;; "cljsjs.object-assign-shim"
                         ])))
