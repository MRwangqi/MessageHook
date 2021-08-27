package com.codelang.messagehook

import android.os.Build
import android.os.Looper
import android.os.Message
import android.os.MessageQueue
import android.text.TextUtils
import android.util.Log

/**
 * Create by codelang in 2021/8/26
 *
 */
object BadTokenUtils {
    private const val EXECUTE_TRANSACTION = 159
    private const val DESTROY_ACTIVITY = 109

    @JvmStatic
    fun isOnDestroyMsgExit(): Boolean {
        val msg = hookMessage()
        return nextMessage(::isOnDestroyMsgExit, msg)
    }


    /**
     * 判断 onDestroy message 是否已存在.
     * 判断 onDestroy message 是否存在需要根据 android 版本进行判断:
     * - Android 8 及之前，onDestroy message 的 what 为 DESTROY_ACTIVITY = 109
     * - Android 8 之后，onDestroy message 的 what 为 EXECUTE_TRANSACTION = 159 并且 obj 的类为 DestroyActivityItem
     * @param msg
     */
    private fun isOnDestroyMsgExit(msg: Message): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            if (msg.what == EXECUTE_TRANSACTION && msg.obj != null) {
                Log.i("BadTokenUtils", "message : ${msg}")
                val clazz = msg.obj::class.java
                if (TextUtils.equals(clazz.name, "android.app.servertransaction.ClientTransaction")) {
                    val method = clazz.getDeclaredMethod("getLifecycleStateRequest")
                    method.isAccessible = true
                   val obj =  method.invoke(msg.obj)
                    if (obj!=null){
                        val clazzName = obj::class.java.name
                        if (TextUtils.equals(clazzName,"android.app.servertransaction.DestroyActivityItem") ){
                            return true
                        }
                    }
                }
            }
        } else {
            return msg.what == DESTROY_ACTIVITY
        }
        return false
    }

    /**
     * hook MessageQueue 中的 Message
     * @return Message
     */
    private fun hookMessage(): Message? {
        val queue: MessageQueue
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // get MessageQueue
            queue = Looper.getMainLooper().queue
        } else {
            val mainLopper: Looper = Looper.getMainLooper()
            val lopperClazz = mainLopper::class.java
            val fieldQueue = lopperClazz.getDeclaredField("mQueue")
            fieldQueue.isAccessible = true
            // get MessageQueue
            queue = fieldQueue.get(mainLopper) as MessageQueue
        }
        val clazz = queue::class.java
        // get Message
        val field = clazz.getDeclaredField("mMessages")
        field.isAccessible = true
        val msg: Message? = field.get(queue) as? Message
        return msg
    }


    /**
     * 遍历 Message.next 是否有符合条件的 msg
     * @param action 条件判断函数
     * @param msg Message
     */
    private fun nextMessage(action: (Message) -> Boolean, msg: Message?): Boolean {
        if (msg == null) {
            return false
        } else {
            if (action(msg)) {
                return true
            }
            // 反射获取 next message
            val clazz = msg::class.java
            val nextField = clazz.getDeclaredField("next")
            nextField.isAccessible = true
            val nextMsg = nextField.get(msg) as? Message
            // 递归
            return nextMessage(action, nextMsg)
        }
    }

}