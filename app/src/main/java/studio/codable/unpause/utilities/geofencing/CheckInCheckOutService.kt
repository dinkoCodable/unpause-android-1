package studio.codable.unpause.utilities.geofencing

import android.app.IntentService
import android.content.Intent
import android.widget.Toast
import androidx.core.app.RemoteInput
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import studio.codable.unpause.model.Shift
import studio.codable.unpause.repository.FirebaseShiftRepository
import studio.codable.unpause.utilities.Constants
import studio.codable.unpause.utilities.manager.SessionManager
import studio.codable.unpause.utilities.networking.Result
import timber.log.Timber
import java.util.*
import kotlin.coroutines.CoroutineContext

class CheckInCheckOutService : IntentService("CheckInCheckOutService"), CoroutineScope {

    private val sessionManager by lazy { SessionManager(this.applicationContext) }
    private val shiftRepository by lazy { FirebaseShiftRepository(
            FirebaseFirestore.getInstance(),
            sessionManager) }

    private val errorHandler = CoroutineExceptionHandler { _, throwable -> Timber.w(throwable) }
    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job + errorHandler

    private lateinit var currentShift : Shift

    @ExperimentalStdlibApi
    override fun onHandleIntent(intent: Intent?) {
        if (intent?.action == Constants.Actions.ACTION_CHECK_IN) {
            checkIn()
        }
        if (intent?.action == Constants.Actions.ACTION_CHECK_OUT) {
            checkOut(getDescription(intent))
        }
    }

    @ExperimentalStdlibApi
    private fun checkIn() {
        if (!sessionManager.isCheckedIn) {
            val newShift = Shift(arrivalTime = Date())
            currentShift = newShift
            launch {
                process(shiftRepository.addNew(newShift)) {
                    sessionManager.isCheckedIn = true
                }
            }
        } else {
            Toast.makeText(applicationContext, "You are already checked in.", Toast.LENGTH_SHORT).show()
        }
    }

    @ExperimentalStdlibApi
    private fun checkOut(description : String?) {
        if (sessionManager.isCheckedIn) {
            val updatedShift = currentShift.copy().addExit(Date(), description ?: "description")
            launch {
                process(shiftRepository.update(updatedShift)) {
                    sessionManager.isCheckedIn = false
                }
            }
        } else {
            Toast.makeText(applicationContext, "You need to check in before you check out",
                Toast.LENGTH_SHORT).show()
        }
    }

    private inline fun <T> process(result: Result<T>, onSuccess: (value: T) -> Unit) {
        when (result) {
            is Result.Success -> {
                Timber.tag(this::class.java.simpleName)
                        .i("Network call successful: ${result.value}")
                onSuccess(result.value)
            }
            is Result.GenericError -> {
                Timber.tag(this::class.java.simpleName).e("Network call failed: $result")
                Toast.makeText(this.applicationContext, result.errorResponse.toString(), Toast.LENGTH_SHORT).show()
            }
            is Result.IOError -> {
                Timber.tag(this::class.java.simpleName).e("Network call failed: network error")
                Toast.makeText(this.applicationContext, "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDescription(intent: Intent): String? {
        return RemoteInput.getResultsFromIntent(intent)?.getString(Constants.Notifications.KEY_DESCRIPTION)
    }


}