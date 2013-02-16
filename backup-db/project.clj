(defproject backup-db "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.google.apis/google-api-services-drive "v2-rev5-1.7.2-beta"]
                 [com.google.http-client/google-http-client "1.10.3-beta"]]
  :repositories [["google-api-services" "http://google-api-client-libraries.appspot.com/mavenrepo"]]
  :main backup-db.core)
