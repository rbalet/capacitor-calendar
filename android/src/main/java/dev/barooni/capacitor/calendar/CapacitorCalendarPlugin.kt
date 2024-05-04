package dev.barooni.capacitor.calendar

import android.Manifest
import android.content.Intent
import android.provider.CalendarContract
import androidx.activity.result.ActivityResult
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback

@CapacitorPlugin(
    name = "CapacitorCalendar",
    permissions = [
        Permission(
            alias = "readCalendar",
            strings = [
                Manifest.permission.READ_CALENDAR,
            ],
        ),
        Permission(
            alias = "writeCalendar",
            strings = [
                Manifest.permission.WRITE_CALENDAR,
            ],
        ),
    ],
)
class CapacitorCalendarPlugin : Plugin() {
    private var implementation = CapacitorCalendar()

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun createEventWithPrompt(call: PluginCall) {
        try {
            implementation.eventIdsArray = implementation.fetchCalendarEventIDs(context)

            val title = call.getString("title", "")
            val calendarId = call.getString("calendarId")
            val location = call.getString("location")
            val startDate = call.getLong("startDate")
            val endDate = call.getLong("endDate")
            val isAllDay = call.getBoolean("isAllDay", false)

            val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)

            intent.putExtra(CalendarContract.Events.TITLE, title)
            calendarId?.let { intent.putExtra(CalendarContract.Events.CALENDAR_ID, it) }
            location?.let { intent.putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
            startDate?.let { intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
            endDate?.let { intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, it) }
            isAllDay?.let { intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, if (it) 1 else 0) }

            return startActivityForResult(
                call,
                intent,
                "openCalendarIntentActivityCallback",
            )
        } catch (error: Exception) {
            call.reject("", "[CapacitorCalendar.${::openCalendarIntentActivityCallback.name}] Could not create the event")
            return
        }
    }

    @ActivityCallback
    private fun openCalendarIntentActivityCallback(
        call: PluginCall?,
        result: ActivityResult,
    ) {
        if (call == null) {
            throw Exception("[CapacitorCalendar.${::createEventWithPrompt.name}] Call is not defined")
        }

        val newEventIds = implementation.getNewEventIds(implementation.fetchCalendarEventIDs(context))
        val newIdsArray = JSArray()
        newEventIds.forEach { id -> newIdsArray.put(id.toString()) }

        val ret = JSObject()
        ret.put("result", newIdsArray)
        call.resolve(ret)
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun checkPermission(call: PluginCall) {
        try {
            val permissionName =
                call.getString("alias")
                    ?: throw Exception("[CapacitorCalendar.${::checkPermission.name}] Permission name is not defined")
            val permissionState =
                getPermissionState(permissionName)
                    ?: throw Exception(
                        "[CapacitorCalendar.${::checkPermission.name}] Could not determine the status of the requested permission",
                    )
            val ret = JSObject()
            ret.put("result", permissionState)
            call.resolve(ret)
        } catch (error: Exception) {
            call.reject("", error.message)
            return
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun checkAllPermissions(call: PluginCall) {
        try {
            return checkPermissions(call)
        } catch (_: Exception) {
            call.reject("", "[CapacitorCalendar.${::checkAllPermissions.name}] Could not determine the status of the requested permissions")
            return
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun requestPermission(call: PluginCall) {
        try {
            val alias =
                call.getString("alias")
                    ?: throw Exception("[CapacitorCalendar.${::requestPermission.name}] Permission name is not defined")
            return requestPermissionForAlias(
                alias,
                call,
                "requestPermissionCallback",
            )
        } catch (error: Exception) {
            call.reject("", error.message)
            return
        }
    }

    @PermissionCallback
    private fun requestPermissionCallback(call: PluginCall) {
        val permissionName = call.getString("alias")
        try {
            val ret = JSObject()
            ret.put("result", getPermissionState(permissionName))
            call.resolve(ret)
        } catch (_: Exception) {
            throw Exception("${::requestPermissionCallback.name} Could not authorize $permissionName")
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun requestAllPermissions(call: PluginCall) {
        try {
            return requestPermissions(call)
        } catch (_: Exception) {
            call.reject("", "[CapacitorCalendar.requestAllPermissions] Could not request permissions")
            return
        }
    }

    @PluginMethod
    fun selectCalendarsWithPrompt(call: PluginCall) {
        call.unimplemented("[CapacitorCalendar.${::selectCalendarsWithPrompt.name}] Not implemented on Android")
        return
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun listCalendars(call: PluginCall) {
        try {
            val calendars = implementation.listCalendars(context)
            val ret = JSObject()
            ret.put("result", calendars)
            call.resolve(ret)
        } catch (_: Exception) {
            call.reject("", "[CapacitorCalendar.${::listCalendars.name}] Failed to get the list of calendars")
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun getDefaultCalendar(call: PluginCall) {
        try {
            val primaryCalendar = implementation.getDefaultCalendar(context)
            val ret = JSObject()
            ret.put("result", primaryCalendar)
            call.resolve(ret)
        } catch (_: Exception) {
            call.reject("", "[CapacitorCalendar.${::getDefaultCalendar.name}] No default calendar found")
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun createEvent(call: PluginCall) {
        try {
            val title =
                call.getString("title")
                    ?: throw Exception("[CapacitorCalendar.${::createEvent.name}] A title for the event was not provided")
            val calendarId = call.getString("calendarId")
            val location = call.getString("location")
            val startDate = call.getLong("startDate")
            val endDate = call.getLong("endDate")
            val isAllDay = call.getBoolean("isAllDay", false)
            val alertOffsetInMinutes = call.getFloat("alertOffsetInMinutes")

            val eventUri =
                implementation.createEvent(
                    context,
                    title,
                    calendarId,
                    location,
                    startDate,
                    endDate,
                    isAllDay,
                    alertOffsetInMinutes,
                )

            val id = eventUri?.lastPathSegment ?: throw IllegalArgumentException("Failed to insert event into calendar")
            val ret = JSObject()
            ret.put("result", id)
            call.resolve(ret)
        } catch (error: Exception) {
            call.reject("", "[CapacitorCalendar.${::createEvent.name}] Unable to create event")
            return
        }
    }

    @PluginMethod
    fun getDefaultRemindersList(call: PluginCall) {
        call.unimplemented("[CapacitorCalendar.${::getDefaultRemindersList.name}] Not implemented on Android")
        return
    }

    @PluginMethod
    fun getRemindersLists(call: PluginCall) {
        call.unimplemented("[CapacitorCalendar.${::getRemindersLists.name}] Not implemented on Android")
        return
    }

    @PluginMethod
    fun createReminder(call: PluginCall) {
        call.unimplemented("[CapacitorCalendar.${::createReminder.name}] Not implemented on Android")
        return
    }

    @PluginMethod(returnType = PluginMethod.RETURN_NONE)
    fun openCalendar(call: PluginCall) {
        val timestamp = call.getLong("date") ?: System.currentTimeMillis()
        try {
            return activity.startActivity(implementation.openCalendar(timestamp))
        } catch (error: Exception) {
            call.reject("", "[CapacitorCalendar.${::openCalendar.name}] Unable to open calendar")
            return
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun listEventsInRange(call: PluginCall) {
        try {
            val startDate =
                call.getLong("startDate")
                    ?: throw Exception("[CapacitorCalendar.${::listEventsInRange.name}] A start date was not provided")
            val endDate =
                call.getLong("endDate")
                    ?: throw Exception("[CapacitorCalendar.${::listEventsInRange.name}] An end date was not provided")
            val ret = JSObject()
            ret.put("result", implementation.listEventsInRange(context, startDate, endDate))
            call.resolve(ret)
        } catch (error: Exception) {
            call.reject("", "[CapacitorCalendar.${::listEventsInRange.name}] Could not get the list of events in requested range")
            return
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun deleteEventsById(call: PluginCall) {
        try {
            val ids =
                call.getArray("ids")
                    ?: throw Exception("[CapacitorCalendar.${::deleteEventsById.name}] Event ids were not provided")
            val ret = JSObject()
            ret.put("result", implementation.deleteEventsById(context, ids))
            call.resolve(ret)
        } catch (error: Exception) {
            call.reject("", "[CapacitorCalendar.${::deleteEventsById.name}] Could not delete events")
            return
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun createCalendar(call: PluginCall) {
        call.unimplemented("[CapacitorCalendar.${::createCalendar.name}] Not implemented on Android")
        return
    }

    @PluginMethod(returnType = PluginMethod.RETURN_PROMISE)
    fun deleteCalendar(call: PluginCall) {
        call.unimplemented("[CapacitorCalendar.${::deleteCalendar.name}] Not implemented on Android")
        return
    }
}
