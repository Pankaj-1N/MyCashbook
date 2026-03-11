package com.mycashbook.app.team;

import android.content.Context;
import java.util.List;

/**
 * Stub interface for team collaboration features (Business Plan)
 * This will be implemented when backend infrastructure is decided
 * (Firebase Realtime DB, Custom API, etc.)
 * 
 * For now, all methods return false/empty to avoid crashes
 */
public class TeamManager {

    private static TeamManager instance;
    private final Context context;

    private TeamManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized TeamManager getInstance(Context context) {
        if (instance == null) {
            instance = new TeamManager(context);
        }
        return instance;
    }

    /**
     * Check if team collaboration is enabled for current user
     * 
     * @return false (stub implementation)
     */
    public boolean isTeamCollaborationEnabled() {
        // TODO: Implement when backend is ready
        return false;
    }

    /**
     * Invite a team member by email
     * 
     * @param bookId The main book ID
     * @param email  Team member email
     * @return false (stub implementation)
     */
    public boolean inviteTeamMember(String bookId, String email) {
        // TODO: Implement when backend is ready
        // Will send invitation email
        // Add member to book's team list
        return false;
    }

    /**
     * Remove a team member
     * 
     * @param bookId The main book ID
     * @param email  Team member email
     * @return false (stub implementation)
     */
    public boolean removeTeamMember(String bookId, String email) {
        // TODO: Implement when backend is ready
        return false;
    }

    /**
     * Get list of team members for a book
     * 
     * @param bookId The main book ID
     * @return empty list (stub implementation)
     */
    public List<String> getTeamMembers(String bookId) {
        // TODO: Implement when backend is ready
        return java.util.Collections.emptyList();
    }

    /**
     * Sync transaction changes with team
     * 
     * @param transactionId Transaction ID
     * @return false (stub implementation)
     */
    public boolean syncTransaction(String transactionId) {
        // TODO: Implement when backend is ready
        // Will push changes to backend
        // Notify other team members
        return false;
    }

    /**
     * Get user who created/modified a transaction
     * 
     * @param transactionId Transaction ID
     * @return null (stub implementation)
     */
    public String getTransactionCreator(String transactionId) {
        // TODO: Implement when backend is ready
        return null;
    }
}
