package com.smartkeyboard.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object LocalQueue {

    private const val PREF_NAME = "text_queue"
    private const val KEY_ENTRIES = "entries"

    fun save(context: Context, text: String, appPackage: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_ENTRIES, "[]")
        val array = JSONArray(existing)
        val obj = JSONObject()
        obj.put("text", text)
        obj.put("app", appPackage)
        obj.put("time", System.currentTimeMillis())
        obj.put("synced", false)
        array.put(obj)
        prefs.edit().putString(KEY_ENTRIES, array.toString()).apply()
    }

    fun getUnsynced(context: Context): List<JSONObject> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ENTRIES, "[]")
        val array = JSONArray(raw)
        val result = mutableListOf<JSONObject>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            if (!obj.getBoolean("synced")) result.add(obj)
        }
        return result
    }

    fun markAllSynced(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ENTRIES, "[]")
        val array = JSONArray(raw)
        for (i in 0 until array.length()) {
            array.getJSONObject(i).put("synced", true)
        }
        prefs.edit().putString(KEY_ENTRIES, array.toString()).apply()
    }
}
