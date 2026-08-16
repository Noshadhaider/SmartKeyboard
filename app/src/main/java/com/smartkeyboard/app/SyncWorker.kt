package com.smartkeyboard.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date

class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val unsynced = LocalQueue.getUnsynced(applicationContext)
        if (unsynced.isEmpty()) return Result.success()

        val db = Firebase.firestore

        return try {
            for (entry in unsynced) {
                val data = hashMapOf(
                    "text"      to entry.getString("text"),
                    "app"       to entry.getString("app"),
                    "timestamp" to Date(entry.getLong("time"))
                )
                db.collection("typed_entries").add(data).await()
            }
            LocalQueue.markAllSynced(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
