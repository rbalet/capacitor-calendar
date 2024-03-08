package dev.barooni.capacitor.calendar

import android.content.ContentResolver
import android.content.Context
import android.provider.CalendarContract
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject

class CapacitorCalendar() {
    var eventsCount: Int = 0

    @Throws(Exception::class)
    fun getTotalEventsCount(context: Context): Int {
        val contentResolver: ContentResolver = context.contentResolver
        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(CalendarContract.Events._ID)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        val count = cursor?.count ?: 0
        cursor?.close()
        return count
    }

    @Throws(Exception::class)
    fun listCalendars(context: Context): JSArray {
        val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        val calendars = JSArray()

        context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
        )?.use { cursor ->
            val idColumnIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
            val nameColumnIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumnIndex)
                val title = cursor.getString(nameColumnIndex)
                val calendar = JSObject().apply {
                    put("id", id)
                    put("title", title)
                }

                calendars.put(calendar)
            }
        } ?: throw Exception("Cursor is null")

        return calendars
    }

    @Throws(Exception::class)
    fun getDefaultCalendar(context: Context): JSObject {
        val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        val selection = "${CalendarContract.Calendars.IS_PRIMARY} = ?"
        val selectionArgs = arrayOf("1")

        context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idColumnIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                val nameColumnIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val id = cursor.getLong(idColumnIndex)
                val title = cursor.getString(nameColumnIndex)

                val calendarObject = JSObject().apply {
                    put("id", id)
                    put("title", title)
                }
                return calendarObject
            } else {
                throw Exception("No primary calendar found")
            }
        }
        throw Exception("No primary calendar found")
    }
}