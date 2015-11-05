(ns jiajiao.learn4clojure)

;心得
;别忘记使用惰性序列。如果产生的结果具有象似性，可以先生成惰性序列，再进行运算。


;Write a function which removes consecutive duplicates from a sequence.
;(= (apply str (__ "Leeeeeerrroyyy")) "Leroy")
(= (
     (fn [s]
       (reduce (fn [c e]
                 (if (not= e (last c))
                   (conj c e)
                   c))
               [] s))

     [1 1 2 3 3 2 2 3]) '(1 2 3 2 3))



;Write a function which duplicates each element of a sequence.
;(= (__ [1 2 3]) '(1 1 2 2 3 3))

(= (

     (fn [c]
       (mapcat (fn [a b] [a b]) c c))

     [1 2 3]) '(1 1 2 2 3 3))

;interleave
;reduce #(conj %1 %2 %2) []



;Write a function which replicates each element of a sequence a variable number of times.
;(= (__ [:a :b] 4) '(:a :a :a :a :b :b :b :b))
(= (
     (fn [c rep]
       (reduce #(apply conj %1 (repeat rep %2)) [] c))

     [1 2 3] 2) '(1 1 2 2 3 3))



;Write a function which creates a list of all integers in a given range.
;(= (__ 1 4) '(1 2 3))

(= (

     (fn [start end]
       (reduce (fn [c e] (conj c (+ start (count c))))
               []
               (repeat (- end start) 0)))

     1 4) '(1 2 3))

(take-while #(< % to)
            (iterate inc from))



;Write a function which separates the items of a sequence by an arbitrary value.
;(= (__ 0 [1 2 3]) [1 0 2 0 3])
(= (
     (fn [e c]
       (drop-last (interleave c (repeat (count c) e))))

     0 [1 2 3]) [1 0 2 0 3])

; 41 Write a function which drops every Nth item from a sequence.
;(= (__ [1 2 3 4 5 6 7 8] 3) [1 2 4 5 7 8])
(= (
     (fn [c i]
       (mapcat
         (fn [e] (if (= (count e) i) (drop-last e) e))
         (partition i i nil c)))

     [1 2 3 4 5 6 7 8] 3) [1 2 4 5 7 8])

#(apply concat (partition-all (dec %2) %2 %1))


;Write a function which calculates factorials.
;(= (__ 3) 6)
;别忘记使用惰性序列
(= (
     (fn fns [i]
       (if (= i 1)
         i
         (* i (fns (dec i)))))

     3) 6)

#(reduce * (range 1 (+ 1 %1)))


;43 Write a function which reverses the interleave process into x number of subsequences.
(= (
     (fn [c i]
       (vals (group-by (fn [e] (rem e i))
                       c)))
     [1 2 3 4 5 6] 2) '((1 3 5) (2 4 6)))

#(apply map list (partition %2 %1))

;44 Write a function which can rotate a sequence in either direction.
;(= (__ 2 [1 2 3 4 5]) '(3 4 5 1 2))
(= (
     (fn [t c]
       (let [tt (if (< t 0) (+ (count c) (rem t (count c))) (rem t (count c)))
             [a b] (split-at tt c)]
         (concat b a)))

     2 [1 2 3 4 5]) '(3 4 5 1 2))

(fn [n xs]
  (let [i (mod n (count xs))]
    (concat (drop i xs) (take i xs))))

;45 Write a higher-order function which flips the order of the arguments of an input function.
;(= 3 ((__ nth) 2 [1 2 3 4 5]))
(= 3 ((
        (fn [infn]
          (fn [& params]
            (apply infn (reverse params))))

        nth) 2 [1 2 3 4 5]))

;49 Write a function which will split a sequence into two parts.
;(= (__ 3 [1 2 3 4 5 6]) [[1 2 3] [4 5 6]])
(= (
     (fn [i c]
       (let [pp (partition-all i c)]
         [(nth pp 0) (apply concat (drop 1 pp))]))

     2 [[1 2] [3 4] [5 6]]) [[[1 2] [3 4]] [[5 6]]])

(fn [n coll] [(take n coll) (drop n coll)])


;50 Write a function which takes a sequence consisting of items with different types and splits them up into a set of homogeneous sub-sequences. The internal order of each sub-sequence should be maintained, but the sub-sequences themselves can be returned in any order (this is why 'set' is used in the test cases).
;(= (set (__ [1 :a 2 :b 3 :c])) #{[1 2 3] [:a :b :c]})
(= (set (
          (fn [c]
            (vals (group-by #(type %) c)))

          [1 :a 2 :b 3 :c])) #{[1 2 3] [:a :b :c]})

;54 Write a function which returns a sequence of lists of x items each. Lists of less than x items should not be returned.
;(= (__ 3 (range 9)) '((0 1 2) (3 4 5) (6 7 8)))
(= (
     (fn pp [i c]
       (when (>= (count c) i)
         (cons (take i c) (pp i (drop i c)))))

     3 (range 9)) '((0 1 2) (3 4 5) (6 7 8)))


;55 Write a function which returns a map containing the number of occurences of each distinct item in a sequence.
;(= (__ [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})
(= (

     (fn [c]
       (into {} (map (fn [v] [(key v) (count (val v))]) (group-by identity c))))

     [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})

(let [m (group-by identity %)] (zipmap (keys m) (map count (vals m))))

;56 Write a function which removes the duplicates from a sequence. Order of the items must be maintained.
;(= (__ [1 2 1 3 1 2 4]) [1 2 3 4])
(= (
     #_(fn [c]
       (keys (group-by identity c)))
     (fn [coll]
       (reduce (fn [c v]
                 (if-not (some #(= v %) c)
                   (conj c v)
                   c)) [] coll))

     [1 2 1 3 1 2 4]) [1 2 3 4])

;58 Write a function which allows you to create function compositions. The parameter list should take a variable number of functions, and create a function that applies them from right-to-left.
;(= [3 2 1] ((__ rest reverse) [1 2 3 4]))
(= [3 2 1] ((
              (fn [& fns]
                (fn [& args]
                  (reduce #(%2 %1)
                          (apply (last fns) args)
                          (rest (reverse fns)))))

              rest reverse) [1 2 3 4]))

;59 Take a set of functions and return a new function that takes a variable number of arguments and returns a sequence containing the result of applying each function left-to-right to the argument list.
(= [21 6 1] ((
               (fn [& fs]
                 (fn [& args]
                   (map #(apply %1 args) fs)))

               + max min) 2 3 5 1 6 4))
;60 Write a function which behaves like reduce, but returns each intermediate value of the reduction. Your function must accept either two or three arguments, and the return sequence must be lazy.
;(= (take 5 (__ + (range))) [0 1 3 6 10])
(= (take 5 (
             (fn [f a & args]
               (reduce () args))

             + (range))) [0 1 3 6 10])