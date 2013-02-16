(ns backup-db.core
  (:require [clojure.java
             [shell :refer (sh with-sh-dir)]
             [io :refer (file)]]
            [backup-db.drive :refer (upload-archive)])
  (:gen-class))

(defn clean []
  (sh "rm" "-r" "dump")
  (sh "rm" "dump.zip"))

(defn create-dump []
  (clean)
  (sh "mongodump")
  (sh "zip" "-r" "dump.zip" "dump"))

(defn archive-name []
  (-> (java.text.SimpleDateFormat. "yyyy-MM-dd_kk-mm")
      (.format (java.util.Date.))
      (str ".zip")))

(defn create-and-upload-dump []
  (with-sh-dir "/tmp"
    (println "Creating dump")
    (create-dump)
    (println "Uploading dump")
    (upload-archive (archive-name) (file "/tmp/dump.zip"))
    (println "Cleaning")
    (clean)
    (println "Finished")))

(defn -main [& args]
  (create-and-upload-dump))

