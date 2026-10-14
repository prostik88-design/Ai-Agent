package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY isInstalled DESC, name ASC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE isInstalled = 1")
    fun getInstalledSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE id = :id LIMIT 1")
    suspend fun getSkillById(id: String): SkillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(skills: List<SkillEntity>)

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Query("UPDATE skills SET isInstalled = :installed WHERE id = :id")
    suspend fun setSkillInstalled(id: String, installed: Boolean)

    @Query("UPDATE skills SET isEnabled = :enabled WHERE id = :id")
    suspend fun setSkillEnabled(id: String, enabled: Boolean)

    @Delete
    suspend fun deleteSkill(skill: SkillEntity)
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY startTimeMillis ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE startTimeMillis >= :startMillis AND endTimeMillis <= :endMillis ORDER BY startTimeMillis ASC")
    fun getEventsInRange(startMillis: Long, endMillis: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE provider = :provider ORDER BY startTimeMillis ASC")
    fun getEventsByProvider(provider: String): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): CalendarEventEntity?

    @Query("SELECT * FROM calendar_events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchEvents(query: String): List<CalendarEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)

    @Query("UPDATE calendar_events SET isNotified = 1 WHERE id = :id")
    suspend fun markNotified(id: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, createdAtMillis DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun searchNotes(query: String): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface McpDao {
    @Query("SELECT * FROM mcp_servers ORDER BY name ASC")
    fun getAllServers(): Flow<List<McpServerEntity>>

    @Query("SELECT * FROM mcp_servers WHERE id = :id LIMIT 1")
    suspend fun getServerById(id: String): McpServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: McpServerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllServers(servers: List<McpServerEntity>)

    @Update
    suspend fun updateServer(server: McpServerEntity)

    @Delete
    suspend fun deleteServer(server: McpServerEntity)

    @Query("SELECT * FROM mcp_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<McpLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: McpLogEntity)

    @Query("DELETE FROM mcp_logs")
    suspend fun clearLogs()
}

@Dao
interface AgentMessageDao {
    @Query("SELECT * FROM agent_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<AgentMessageEntity>>

    @Query("SELECT * FROM agent_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int = 10): List<AgentMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AgentMessageEntity): Long

    @Query("DELETE FROM agent_messages")
    suspend fun clearChat()
}

@Dao
interface AgentTaskDao {
    @Query("SELECT * FROM agent_tasks ORDER BY nextRunMillis ASC")
    fun getAllTasks(): Flow<List<AgentTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AgentTaskEntity): Long

    @Update
    suspend fun updateTask(task: AgentTaskEntity)

    @Delete
    suspend fun deleteTask(task: AgentTaskEntity)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAtMillis DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}

@Dao
interface DirectChatMessageDao {
    @Query("SELECT * FROM direct_chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<DirectChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DirectChatMessageEntity): Long

    @Query("DELETE FROM direct_chat_messages")
    suspend fun clearMessages()
}
