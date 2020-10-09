package com.gooselab.gupta4_kotlin

class Post {

    var postId = ""
    var writerId = ""
    var title = ""
    var message = ""
    var writeTime:Any = Any()
    var commentCount = 0
    var hitsCount = 0
    var likesCount = 0
    var nickname = "익명"
    var board = ""
    var commentIdMap: HashMap<String, String> = HashMap()
}