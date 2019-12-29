(defproject montag "0.1.3-SNAPSHOT"
  :description "Manage your books with style"
  :url "http://github.com/wandersoncferreira/montag"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.4.2"]
                 [org.martinklepsch/clj-http-lite "0.4.3"]
                 [cheshire "5.9.0"]
                 [org.clojure/data.xml "0.0.8"]]
  :main montag.core
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.skip-macros=true"]}
             :dev {:plugins [[lein-shell "0.5.0"]]}}
  :aliases
  {"native"
   ["shell"
    "native-image"
    "-jar" "./target/${:uberjar-name:-${:name}-${:version}-standalone.jar}"
    "--report-unsupported-elements-at-runtime"
    "--enable-url-protocols=http,https"
    "--initialize-at-build-time"
    "--enable-all-security-services"
    "--no-server"
    "--allow-incomplete-classpath"
    "-Dfile.encoding=UTF-8"
    "-H:+AllowVMInspection"
    "-H:ReflectionConfigurationFiles=reflection.json"
    "-H:Name=./target/${:name}"]}
  :repl-options {:init-ns montag.core})
