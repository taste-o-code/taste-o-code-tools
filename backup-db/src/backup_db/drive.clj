(ns backup-db.drive
  (:import [com.google.api.client.googleapis.auth.oauth2 GoogleCredential GoogleTokenResponse GoogleAuthorizationCodeFlow$Builder GoogleRefreshTokenRequest]
           [com.google.api.client.http FileContent HttpTransport FileContent]
           [com.google.api.client.http.javanet NetHttpTransport]
           [com.google.api.client.json.jackson JacksonFactory]
           [com.google.api.services.drive Drive$Builder DriveScopes]
           [com.google.api.services.drive.model File ParentReference]))

(def client-id "")
(def client-secret "")
(def redirect-uri "urn:ietf:wg:oauth:2.0:oob")
(def refresh-token "")
(def flow (.. (GoogleAuthorizationCodeFlow$Builder.
                  (NetHttpTransport.)
                  (JacksonFactory.)
                  client-id
                  client-secret
                  [DriveScopes/DRIVE])
                 (setAccessType "offline")
                 (setApprovalPrompt "force")
                 (build)))

(def ^:dynamic *drive*)

(defn get-authorize-code []
  (let [url (.. flow newAuthorizationUrl (setRedirectUri redirect-uri) build)]
    (println "Please open the following URL in your browser then type the authorization code:")
    (println " " url)))

(defn accept-code [code]
  (.. flow (newTokenRequest code) (setRedirectUri redirect-uri) execute toPrettyString))

(defn get-credentials []
  (let [response (.execute (GoogleRefreshTokenRequest. (NetHttpTransport.) (JacksonFactory.) refresh-token client-id client-secret))]
    (.setFromTokenResponse (GoogleCredential.) response)))

(defn get-drive []
  (.build (Drive$Builder. (NetHttpTransport.) (JacksonFactory.) (get-credentials))))

(defmacro with-drive [& body]
  `(binding [*drive* (get-drive)]
     ~@body))

(defn insert
  ([file content]
     (.. *drive* files (insert file content) execute))
  ([file]
     (.. *drive* files (insert file) execute)))

(defn create-folder [name]
  (let [folder (doto (File.)
                 (.setTitle name)
                 (.setMimeType "application/vnd.google-apps.folder"))]
    (insert folder)))

(defn file-list []
  (.. *drive* files list execute getItems))

(defn get-or-create-folder [name]
  (let [files (file-list)
        satisfy? (fn [f] (and (= (.getTitle f) name)
                              (not (.getTrashed (.getLabels f)))))]
    (if-let [res (first (filter satisfy? files))]
      res
      (create-folder name))))

(defn upload-archive [name file]
  (with-drive
    (let [folder (get-or-create-folder "backup")
          drive-file (doto (File.)
                       (.setTitle name)
                       (.setParents [(doto (ParentReference.) (.setId (.getId folder)))])
                       (.setMimeType "application/zip"))
          content (FileContent. "application/zip" file)]
      (insert drive-file content))))
