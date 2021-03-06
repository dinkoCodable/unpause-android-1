package studio.codable.unpause.utilities.manager

import androidx.annotation.StringRes
import androidx.core.util.Pair
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.datepicker.MaterialDatePicker
import studio.codable.unpause.R
import studio.codable.unpause.base.activity.BaseActivity
import studio.codable.unpause.screens.fragment.confirmDialog.ConfirmDialogFragment
import studio.codable.unpause.screens.fragment.datePicker.DatePickerFragment
import studio.codable.unpause.screens.fragment.datePicker.DatePickerListener
import studio.codable.unpause.screens.fragment.descriptionDialog.DescriptionDialogFragment
import studio.codable.unpause.screens.fragment.workingTimeWarning.WorkingTimeWarningFragment
import studio.codable.unpause.utilities.LambdaDoubleIntToUnit
import studio.codable.unpause.utilities.LambdaNoArgumentsUnit
import studio.codable.unpause.utilities.LambdaStringToUnit
import studio.codable.unpause.screens.fragment.timePicker.TimePickerFragment
import studio.codable.unpause.screens.fragment.upgradeToPremium.UpgradeToPremiumFragment
import java.util.*

class DialogManager(private val context: BaseActivity) {
    private lateinit var descriptionDialogFragment: DescriptionDialogFragment
    private lateinit var workingTimeWarningFragment: WorkingTimeWarningFragment
    private val datePickerFragment: DatePickerFragment by lazy { DatePickerFragment() }
    private lateinit var timePickerFragment: TimePickerFragment
    private lateinit var confirmDialogFragment: ConfirmDialogFragment
    private val upgradeToPremiumFragment: UpgradeToPremiumFragment by lazy { UpgradeToPremiumFragment() }

    fun openDescriptionDialog(
        @StringRes title: Int,
        @StringRes description: Int?,
        isFullscreen: Boolean,
        dialogListenerOnSave: LambdaStringToUnit,
        dialogListenerOnCancel: LambdaNoArgumentsUnit?
    ) {
        openDescriptionDialog(
            context.getString(title),
            description?.let { context.getString(it) },
            isFullscreen, dialogListenerOnSave, dialogListenerOnCancel
        )
    }

    fun openDescriptionDialog(
        title: String,
        description: String?,
        isFullscreen: Boolean,
        dialogListenerOnSave: LambdaStringToUnit,
        dialogListenerOnCancel: LambdaNoArgumentsUnit?
    ) {
        descriptionDialogFragment = DescriptionDialogFragment().apply {
            setTitle(title)
            description?.let { setDescription(description) }
            setOnSaveListener(dialogListenerOnSave)
            if (dialogListenerOnCancel != null) {
                setOnCancelListener(dialogListenerOnCancel)
            }
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.full_screen_dialog)
        }
        if (isFullscreen) {
            showFullscreenDialog(descriptionDialogFragment)
        } else {
            descriptionDialogFragment.show(context.supportFragmentManager, "Edit dialog fragment")
        }
    }

    fun openWorkingTimeDialog(
            arrivalTime: Date,
            exitTime: Date,
            isArrivalDateEditable: Boolean,
            dialogManager: DialogManager,
            dialogListener: WorkingTimeWarningFragment.DialogListener
    ) {
        workingTimeWarningFragment =
            WorkingTimeWarningFragment(arrivalTime, exitTime, isArrivalDateEditable, dialogManager)
        workingTimeWarningFragment.addListener(dialogListener)
        showFullscreenDialog(workingTimeWarningFragment)
    }

    fun openDatePickerDialog(datePickerListener: DatePickerListener) {
        datePickerFragment.setListener(datePickerListener)
        datePickerFragment.show(context.supportFragmentManager, "Date picker fragment")
    }

    fun openTimePickerDialog(hour: Int, minute: Int, timePickerListener: LambdaDoubleIntToUnit) {
        timePickerFragment = TimePickerFragment(hour, minute)
        timePickerFragment.setListener(timePickerListener)
        timePickerFragment.show(context.supportFragmentManager, "Time picker fragment")
    }

    fun openConfirmDialog(@StringRes message : Int, confirmDialogListener: LambdaNoArgumentsUnit) {
        confirmDialogFragment = ConfirmDialogFragment(context.getString(message))
        confirmDialogFragment.addListener(confirmDialogListener)
        confirmDialogFragment.show(context.supportFragmentManager, "Confirm dialog fragment")
    }

    fun openDateRangePickerDialog(
        from: Long? = Calendar.getInstance().timeInMillis,
        to: Long? = Calendar.getInstance().timeInMillis,
        listener: (Pair<Long, Long>) -> Unit
    ) {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setSelection(Pair(from, to))
        builder.build().apply {
            addOnPositiveButtonClickListener { listener(it) }
            addOnNegativeButtonClickListener { dismiss() }
            addOnCancelListener { dismiss() }
        }.show(context.supportFragmentManager, "Date range picker fragment")
    }

    fun openUpgradeToPremiumDialog(onBuySubscription1Listener : LambdaNoArgumentsUnit, onBuySubscription2Listener : LambdaNoArgumentsUnit) {
        upgradeToPremiumFragment.setOnBuySubscription1Listener(onBuySubscription1Listener)
        upgradeToPremiumFragment.setOnBuySubscription2Listener(onBuySubscription2Listener)
        showFullscreenDialog(upgradeToPremiumFragment)
    }

    private fun showFullscreenDialog(dialogFragment: DialogFragment) {
        val transaction = context.supportFragmentManager.beginTransaction()
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction
                .add(android.R.id.content, dialogFragment)
                .addToBackStack(null)
                .commit()
    }

}