(set-env!
  :source-paths #{"src/clj" "src/cljs"}
  :dependencies '[[adzerk/boot-cljs "1.7.228-2" :scope "test"]
                  [adzerk/boot-reload "0.4.12" :scope "test"]
                  [org.clojure/test.check "0.9.0" :scope "test"]
                  ; cljs deps
                  [org.clojure/clojurescript "1.9.518" :scope "test"]
                  [paren-soup "2.8.6" :scope "test"]
                  [cljsjs/codemirror "5.19.0-0" :scope "test"]
                  [reagent "0.6.0" :exclusions [cljsjs/react cljsjs/react-dom] :scope "test"]
                  [cljs-react-material-ui "0.2.34" :scope "test"]
                  ; clj deps
                  [org.clojure/clojure "1.8.0"]
                  [ring/ring-core "1.5.1"]
                  [ring-basic-authentication "1.0.5"]
                  [http-kit "2.2.0"]
                  [compliment "0.3.1"]
                  [eval-soup "1.2.0"]]
  :repositories (conj (get-env :repositories)
                  ["clojars" {:url "https://clojars.org/repo/"
                              :username (System/getenv "CLOJARS_USER")
                              :password (System/getenv "CLOJARS_PASS")}]))

(require
  '[adzerk.boot-cljs :refer [cljs]]
  '[adzerk.boot-reload :refer [reload]]
  '[nightlight.core :refer [dev-start]]
  '[clojure.spec.test :refer [instrument]])


(task-options!
  pom {:project 'org.clojars.chpill/nightlight
       :version "1.6.5-SNAPSHOT"
       :description "An embedded Clojure editor"
       :url "https://github.com/chpill/Nightlight"
       :license {"Public Domain" "http://unlicense.org/UNLICENSE"}}
  push {:repo "clojars"}
  sift {:include #{#"nightlight-public/main.out"}
        :invert true})

(deftask local []
  (set-env! :resource-paths #{"src/clj" "src/cljs" "resources" "prod-resources"})
  (comp (cljs :optimizations :advanced) (sift) (pom) (jar) (install)))

(deftask deploy []
  (set-env! :resource-paths #{"src/clj" "src/cljs" "resources" "prod-resources"})
  (comp (cljs :optimizations :advanced) (sift) (pom) (jar) (push)))

(deftask make-jar []
  (set-env! :resource-paths #{"src/clj" "src/cljs" "resources" "prod-resources"})
  (comp (cljs :optimizations :advanced) (sift) (pom) (jar) (target)))

;; Copied from bootlaces
;; https://github.com/adzerk-oss/bootlaces/blob/master/src/adzerk/bootlaces.clj#L39-L54
(deftask authorize
  "Collect CLOJARS_USER and CLOJARS_PASS from the user if they're not set."
  []
  (fn [next-handler]
    (fn [fileset]
      (let [clojars-creds (atom {})]
        (do (println "CLOJARS_USER and CLOJARS_PASS were not set; please enter your Clojars credentials.")
            (print "Username: ")
            (#(swap! clojars-creds assoc :username %) (read-line))
            (print "Password: ")
            (#(swap! clojars-creds assoc :password %)
             (apply str (.readPassword (System/console)))))
        (merge-env! :repositories [["deploy-clojars" (merge @clojars-creds {:url "https://clojars.org/repo"})]])
        (next-handler fileset)))))

(deftask run []
  (set-env! :resource-paths #{"src/clj" "src/cljs" "resources" "dev-resources"})
  (comp
    (watch)
    (reload :asset-path "nightlight-public")
    (cljs :source-map true :optimizations :none)
    (with-pass-thru _
      (instrument)
      (dev-start {:port 4000 :url "http://localhost:4000"}))
    (target)))

