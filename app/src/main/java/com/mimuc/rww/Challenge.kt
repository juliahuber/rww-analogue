package com.mimuc.rww

import Category
import java.io.Serializable

class Challenge(
    var title: String? = null,
    var cat:Category? = null,
    var personalized: Boolean? = null,
    var time: String? = null,
    var date: String? = null,
    var notification: Notification? = null,
    var viaNotification: Boolean? = false,
    var answer: String? = null,
    var reason: String? = null,
/**
    var sadHappy: String? = null,
    var boredExcited: String? = null,
    var annoyedPleased: String? = null,
    var balanceAfter: String? = null,
**/
//    var agreeHelped: String? = null,
    var agreeAwareness: String? = null,
    var agreeEnjoyed: String? = null,
    var agreeBored: String? = null,
    var agreeHappy: String? = null,
    var agreeAnnoyed: String? = null,
    //var agreeSame: String? = null,
    var agreeWellbeing: String? = null,
    var agreeBalance: String? = null,
    //var balanceBefore: String? = null,
    var whichContext: String? = null,
    var timeSinceUnlock: String? = null,
    var lastUsedApps: String? = null,
    //var recognized: String? = null,
    var feltOverload: Boolean? = null,
    var endTime: String? = null


) : Serializable

