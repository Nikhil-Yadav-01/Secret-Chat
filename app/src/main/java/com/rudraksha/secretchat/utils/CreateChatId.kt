package com.rudraksha.secretchat.utils

import com.rudraksha.secretchat.data.model.ChatType

fun createChatId(usernames: List<String>, chatType: ChatType = ChatType.PRIVATE): String {
    return "${chatType.name}^" + usernames.sorted().joinToString(separator = "^")
}

//fun isUserInChat(chatId: String, username: String): Boolean {
//    return chatId.split("^").contains(username)
//}
//
//fun getParticipants(chatId: String): MutableList<String> {
//    val list = chatId.split("^").toMutableList()
//    list.removeAt(0)
//    return list
//}
//
//fun getReceivers(chatId: String, username: String): String {
//    val list = getParticipants(chatId)
//    if (list.size > 1) list.remove(username)
//    return list.sorted().joinToString(",")
//}

fun getReceivers(participants: String, username: String): List<String> {
    val list = participants.split(",").toMutableList()
    if (list.size > 1) list.remove(username)
    return list
}