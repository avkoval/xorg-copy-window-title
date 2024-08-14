#!/usr/bin/env bb
(ns xorg-copy-window-title
  (:require
   [babashka.process :refer [shell process]]
   [clojure.string :as str]
   [clojure.java.io :as io]
   )
  )

(defn xwininfo [id]
  (into {} (remove nil?
                   (for [line (-> (shell {:out :string} (str "xwininfo -id " id))
                                  :out
                                  str/split-lines
                                  )]
                     (let [index_of_colon (str/index-of line ":")] 
                       (cond 
                         (str/starts-with? line "xwininfo: Window id:") {:title (str/replace (subs line 32) "\"" "")}
                         (not (nil? index_of_colon)) (hash-map (str/trim (subs line 0 index_of_colon)) (str/trim (subs line (+ 1 index_of_colon))))
                         ))
                     )))
  )

(defn get-active-window []
  (str "0x" (str/replace (format "%8x" (Integer/parseInt (str/trim (:out (shell {:out :string} (str "xdotool getwindowfocus")))))) #"\s" "0"))
  )

(comment
  (let [title "[FH-9202] BE. Implement jQuery solution for Django template HTML object display - Jira - Google Chrome"
        filtered (str/replace title #" - Jira.*" "")
        ]
    filtered
    )
  (str/replace "Konqueror - Програми KDE — Mozilla Firefox" #"— Mozilla Firefox.*" "")
  )


(defn -main [& _]
  (let [clipboard (process "xclip -selection clipboard")
        clipboard_in (io/writer (:in clipboard)) 
        title (:title (xwininfo (get-active-window)))
        filtered_title (-> title
                          (str/replace #" - Jira.*" "")
                          (str/replace #" - Google.*" "")
                          (str/replace #" - Konqueror.*" "")
                          (str/replace #" — Mozilla Firefox.*" "")
                          )
        ]
    (binding [*out* clipboard_in]
      (print filtered_title))
    (.close clipboard_in)
    )
  )

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
