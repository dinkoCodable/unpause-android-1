package studio.codable.unpause.model.firestore

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName
import studio.codable.unpause.model.Shift
import studio.codable.unpause.model.User
import java.util.HashMap

data class FirestoreUser(
    @DocumentId
    var documentId: String = "",
    var email: String = "",
    var firstName: String? = "",
    var lastName: String? = "",
    var shifts: List<FirestoreShift>? = null,
    var companyReference: DocumentReference? = null,
    /*custom getter/setter names Firebase expects when deserializing object, but Kotlin
       generates other ones*/
    @get:JvmName("getIsPromoUser")
    @set:JvmName("setIsPromoUser")
    var isPromoUser: Boolean = false
) {
    companion object {
        const val FIRST_NAME = "firstName"
        const val LAST_NAME = "lastName"
        const val SHIFTS = "shifts"
    }

    constructor(user: User) : this() {
        this.documentId = user.id
        this.email = user.email
        this.firstName = user.firstName
        this.lastName = user.lastName
        this.shifts = user.shifts?.map { FirestoreShift(it) }
    }

    fun toUser(): User {
        return User(documentId, email, firstName, lastName,companyId = companyReference?.id, isPromoUser = isPromoUser ?: false)
    }

    fun extractShifts(): List<Shift> {
        return shifts?.map { fs ->
            Shift(
                arrivalTime = fs.arrivalTime?.toDate(),
                exitTime = fs.exitTime?.toDate(),
                description = fs.description
            )
        } ?: arrayListOf()
    }

    fun asHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            FIRST_NAME to firstName,
            LAST_NAME to lastName,
            SHIFTS to shifts
        )
    }
}