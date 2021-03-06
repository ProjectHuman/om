(ns om.core)

(defmacro pure [obj children]
  `(om.core/Pure. ~obj ~children))

(defmacro component
  "Sugar over reify for quickly putting together components that
   only need to implement om.core/IRender and don't need access to
   the owner argument."
  [& body]
  `(reify
     om.core/IRender
     (~'render [this#]
       ~@body)))

(defmacro allow-reads [& body]
  `(binding [om.core/*read-enabled* true]
    ~@body))

(defmacro check [& body]
  `(if om.core/*read-enabled*
     (do
       ~@body)
     (throw
       (js/Error.
         (str "Cannot manipulate cursor outside of render phase, only "
              "om.core/transact!, om.core/update!, and om.core/read operations allowed")))))

(defmacro safe-transact! [cursor korks f & args]
  `(let [path#  (.-path ~cursor)
         state# (.-state ~cursor)]
     (if-not (sequential? ~korks)
       (swap! state# update-in (conj path# ~korks) ~f ~@args)
       (swap! state# update-in (into path# ~korks) ~f ~@args))))

(defmacro safe-update! [cursor f & args]
  `(let [path#  (.-path ~cursor)
         state# (.-state ~cursor)]
     (if (empty? path#)
       (swap! state# #(~f % ~@args))
       (swap! state# update-in path# ~f ~@args))))
