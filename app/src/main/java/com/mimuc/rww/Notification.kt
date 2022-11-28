package com.mimuc.rww

import Category
import java.io.Serializable

class Notification(
    var trigger: String? = null,
    var triggerTime:String? = null
) : Serializable

