(ns montag.notes
  (:require [clojure.string :as cstr]))


(def text-string (slurp "/home/wand/readings/oop-in-common-lisp.asc"))

text-string
;; => "book_id: 1175730\ntags: oop, common-lisp, inheritance, classes, objects, interface, protocols\n\n=== classes\n\nYou should define one or more basic classes and build more\nspecialized classes on them\n\n=== inheritance\n\ninheritance allows the design and implementation of an\napplication program to highly modular, and obviates the need\nfor maintaing several bodies or nearly identical code\n\n=== inheritance\n\ninheritance is the sharing of characteristics and behavior\namong a set of classes.\n\n=== interface\n\nthe clients of your program should depend on only the\ninterface, which is a high-level description of operations\nthat can be performed on a set of objects.\n\n=== benefits\n\n1. the program mode closely resembles the world it is modeling\n2. client programs benefit from a well-defined interface\n3. the programmer benefits from a modular implementation\n4. the programmer benefits from a extensible program\n\n=== classes\n\nA class is a new type of data structure. A class is a\ntype. Each individual object of that type is an instance of\nthe class. Each instance of a given class has the same\nstructure, behavior, and type as do the other instances of\nthe class.\n\n=== protocols\n\nA generic function specifies only the interface. The\nimplementation of a generic function does not exist in one\nplace; it is distributed across a set of methods.\n\n=== protocols\n\nthe definition of a generic function establishes a parameter\npattern that must be followed by all methods for that\ngeneric function.\n\n=== protocols\n\na generic function defines the interface of a single\noperation. This is a valuable concept in the initial design\nphase, because it helps you focus on the interface while\nleaving the details of the implementation until later.\n\n=== protocols\n\n1. Restrict the user's access to the internal data structures\n2. Provide constructor function for creating the data structures\n3. Design the protocol to anticipate the needs for the users\n4. Allow the protocol to evolve to meet the reasonable needs of users\n5. Design some protocols to be extensible by the user\n\n=== method-roles\n\nIn CLOS, methods has specific roles such as primary method,\nbefore-methods and after-methods.\n"

(defn get-tags [text-string]
  (-> #"tags:.*"
      (re-find text-string)
      (cstr/split #"tags: ")
      second
      (cstr/split #",")))
