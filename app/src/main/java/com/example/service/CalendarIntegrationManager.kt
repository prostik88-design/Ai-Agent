package com.example.service

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.example.data.dao.CalendarEventDao
import com.example.data.model.CalendarEventEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarIntegrationManager(
    private val context: Context,
    private val calendarEventDao: CalendarEventDao,
    private val notificationDispatcher: NotificationDispatcher
) {

    fun getAllEvents(): Flow<List<CalendarEventEntity>> = calendarEventDao.getAllEvents()

    fun getEventsByProvider(provider: String): Flow<List<CalendarEventEntity>> =
        calendarEventDao.getEventsByProvider(provider)

    suspend fun createEvent(
        title: String,
        description: String = "",
        startTimeMillis: Long,
        endTimeMillis: Long,
        location: String = "",
        provider: String = "GOOGLE",
        reminderMinutes: Int = 15,
        attendees: String = ""
    ): CalendarEventEntity {
        val event = CalendarEventEntity(
            title = title,
            description = description,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            location = location,
            provider = provider.uppercase(),
            reminderMinutesBefore = reminderMinutes,
            attendees = attendees
        )
        val insertedId = calendarEventDao.insertEvent(event)
        return event.copy(id = insertedId)
    }

    suspend fun updateEvent(event: CalendarEventEntity) {
        calendarEventDao.updateEvent(event)
    }

    suspend fun deleteEvent(id: Long) {
        calendarEventDao.deleteEventById(id)
    }

    suspend fun deleteEventByTitle(query: String): Boolean {
        val matches = calendarEventDao.searchEvents(query)
        return if (matches.isNotEmpty()) {
            calendarEventDao.deleteEventById(matches.first().id)
            true
        } else {
            false
        }
    }

    suspend fun searchEvents(query: String): List<CalendarEventEntity> {
        return calendarEventDao.searchEvents(query)
    }

    fun launchSystemCalendarInsert(event: CalendarEventEntity) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, event.title)
                putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startTimeMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endTimeMillis)
                putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                if (event.attendees.isNotBlank()) {
                    putExtra(Intent.EXTRA_EMAIL, event.attendees)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handled gracefully if no calendar app
        }
    }

    fun triggerMeetingAlert(event: CalendarEventEntity) {
        val timeFormat = SimpleDateFormat("HH:mm, dd MMMM", Locale("ru"))
        val startFormatted = timeFormat.format(Date(event.startTimeMillis))
        val details = "Встреча «${event.title}» начнется в $startFormatted (${event.location.ifBlank { "Онлайн" }})"
        notificationDispatcher.dispatchMeetingNotification(
            title = event.title,
            details = details,
            provider = event.provider,
            notificationId = event.id.toInt()
        )
    }
}
