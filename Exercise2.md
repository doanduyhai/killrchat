Exercise 2
==========

 First checkout the branch **exercise_2_specs** with Git: `git checkout exercise_2_specs`

 In this exercise, we are going to manage chat room creation and listing

 The **`ChatRoomEntity`** is already defined with proper annotations. For the chat room creator property, instead of
storing just the user login and requiring additional "SELECT" to fetch the creator information, we **denormalize**
the data by storing a **`LightUserModel`**


 Notice the **`@JSON`** annotation on some attributes. This annotation tells **Achilles** to serialize and save
the attribute value as a JSON string when the attribute type is not one of the supported Cassandra native type.
The JSON encoding/decoding is performed automatically by **Achilles** at runtime.

 For more detail about JSON serialization, you can check the doc: https://github.com/doanduyhai/Achilles/wiki/JSON-Serialization



 To complete the exercise, open the **`ChatRoomService`** and implement all the methods to make the tests greens. Follow the
instructions and read the documentation link ( **if necessary, don't spend too much time on it** )


Solution
========

 The solution is given in the branch **exercise_2_solution**.

 Do not forget to save your work (if you want to keep track
 of your solution) in a branch and then check out with Git: `git checkout exercise_2_solution`



