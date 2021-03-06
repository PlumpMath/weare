
(ns weare.web.core
  (:use compojure.core
        [weare.web.auth :only [wrap-login wrap-user]]
        (ring.middleware [lint :only [wrap-lint]]
                         [reload :only [wrap-reload]]
                         [stacktrace :only [wrap-stacktrace]]))
  (:require (compojure [handler :as handler]
                       [route :as route])
            (weare.web [pages :as pages]
                       [actions :as actions])))

(defroutes user-routes
  (GET "/" [] "USER PAGE")
  (GET "/sources" [] pages/sources)
  (POST "/sources" [] actions/source-create))

(defroutes app-routes

  (GET "/" [] pages/home)

  (context "/user" []
    (wrap-login user-routes))

  (GET "/login" [] pages/login)
  (POST "/login" [] actions/user-login)
  (ANY "/logout" [] actions/user-logout)

  (route/resources "/assets")
  (route/not-found pages/not-found)

)

(def dev? true)

(defn wrap-if [handler pred wrapper & args]
  (if pred
    (apply wrapper handler args)
    handler))

(def app
  (-> (wrap-user app-routes)
    (handler/site :session)
    (wrap-if dev? wrap-lint)
    (wrap-if dev? wrap-stacktrace)
    (wrap-if dev? wrap-reload)))

