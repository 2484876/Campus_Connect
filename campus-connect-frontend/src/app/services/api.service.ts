import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  PostDTO, CommentDTO, UserDTO, ConnectionDTO,
  MessageDTO, ConversationDTO, NotificationDTO,
  EventDTO, EventAttendeeDTO, EventChatMessageDTO, CreateEventRequest, RsvpStatus,
  PageResponse, CommunityDTO, CommunityPostDTO, CommunityCommentDTO,
  CommunityResourceDTO, CreateResourceRequest,
  ExperienceDTO, MessageDeleteDTO, ReactionDTO,
  HashtagDTO, ReportRequest, ReportDTO, SearchResultDTO,
  PollDTO, CreatePollRequest,
  KudosDTO, CreateKudosRequest,
  AchievementDTO, AchievementStats, StreakDTO,
  StoryDTO, UserStoriesGroupDTO, CreateStoryRequest, StoryViewer,
  ConnectionSuggestionDTO, MutualConnectionDTO,
  SkillEndorsementDTO, CreateEndorsementRequest, EndorsementSummaryDTO,
  ProfileCompletionDTO, SkillSuggestion
} from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {

  private api = environment.apiUrl;

  constructor(private http: HttpClient) { }

  uploadImage(file: File): Observable<{ url: string }> {
    const fd = new FormData(); fd.append('file', file);
    return this.http.post<{ url: string }>(`${this.api}/upload/image`, fd);
  }
  uploadVideo(file: File): Observable<{ url: string }> {
    const fd = new FormData(); fd.append('file', file);
    return this.http.post<{ url: string }>(`${this.api}/upload/video`, fd);
  }
  uploadAvatar(file: File): Observable<{ url: string }> {
    const fd = new FormData(); fd.append('file', file);
    return this.http.post<{ url: string }>(`${this.api}/upload/avatar`, fd);
  }
  updateAvatar(file: File): Observable<UserDTO> {
    const fd = new FormData(); fd.append('file', file);
    return this.http.post<UserDTO>(`${this.api}/users/me/avatar`, fd);
  }

  getFeed(page: number, size = 10, mode: 'ALL' | 'FOR_YOU' = 'ALL'): Observable<PageResponse<PostDTO>> {
    return this.http.get<PageResponse<PostDTO>>(`${this.api}/feed?page=${page}&size=${size}&mode=${mode}`);
  }
  getPublicFeed(page: number): Observable<PageResponse<PostDTO>> {
    return this.http.get<PageResponse<PostDTO>>(`${this.api}/feed/public?page=${page}`);
  }
  createPost(data: any): Observable<PostDTO> {
    return this.http.post<PostDTO>(`${this.api}/posts`, data);
  }
  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/posts/${id}`);
  }
  toggleLike(postId: number): Observable<any> {
    return this.http.post(`${this.api}/posts/${postId}/like`, {});
  }
  addComment(postId: number, content: string): Observable<CommentDTO> {
    return this.http.post<CommentDTO>(`${this.api}/posts/${postId}/comments`, { content });
  }
  getComments(postId: number, page = 0): Observable<PageResponse<CommentDTO>> {
    return this.http.get<PageResponse<CommentDTO>>(`${this.api}/posts/${postId}/comments?page=${page}`);
  }

  getProfile(): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.api}/users/me`);
  }
  getUser(id: number): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.api}/users/${id}`);
  }
  updateProfile(data: any): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.api}/users/me`, data);
  }
  searchUsers(q: string, page = 0): Observable<PageResponse<UserDTO>> {
    return this.http.get<PageResponse<UserDTO>>(`${this.api}/users/search?q=${q}&page=${page}`);
  }
  getSuggestions(page = 0): Observable<PageResponse<UserDTO>> {
    return this.http.get<PageResponse<UserDTO>>(`${this.api}/users/suggestions?page=${page}`);
  }
  uploadUserBanner(file: File): Observable<UserDTO> {
    const fd = new FormData(); fd.append('file', file);
    return this.http.post<UserDTO>(`${this.api}/users/me/banner`, fd);
  }
  addExperience(data: any): Observable<ExperienceDTO> {
    return this.http.post<ExperienceDTO>(`${this.api}/users/me/experience`, data);
  }
  deleteExperience(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/users/me/experience/${id}`);
  }
  getUsersByRole(role: string, page = 0): Observable<PageResponse<UserDTO>> {
    return this.http.get<PageResponse<UserDTO>>(`${this.api}/users/by-role?role=${role}&page=${page}`);
  }
  getCelebrants(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(`${this.api}/users/celebrants`);
  }

  sendConnectionRequest(receiverId: number, message?: string): Observable<ConnectionDTO> {
    const body: any = { receiverId };
    if (message && message.trim()) body.message = message.trim();
    return this.http.post<ConnectionDTO>(`${this.api}/connections/request`, body);
  }
  acceptConnection(id: number): Observable<ConnectionDTO> {
    return this.http.put<ConnectionDTO>(`${this.api}/connections/${id}/accept`, {});
  }
  rejectConnection(id: number): Observable<void> {
    return this.http.put<void>(`${this.api}/connections/${id}/reject`, {});
  }
  removeConnection(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/connections/${id}`);
  }
  withdrawConnection(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/connections/${id}/withdraw`);
  }
  getConnections(page = 0): Observable<PageResponse<ConnectionDTO>> {
    return this.http.get<PageResponse<ConnectionDTO>>(`${this.api}/connections?page=${page}`);
  }
  getPendingRequests(page = 0): Observable<PageResponse<ConnectionDTO>> {
    return this.http.get<PageResponse<ConnectionDTO>>(`${this.api}/connections/pending?page=${page}`);
  }
  getSentRequests(page = 0): Observable<PageResponse<ConnectionDTO>> {
    return this.http.get<PageResponse<ConnectionDTO>>(`${this.api}/connections/sent?page=${page}`);
  }
  getConnectionSuggestions(limit = 10): Observable<ConnectionSuggestionDTO[]> {
    return this.http.get<ConnectionSuggestionDTO[]>(`${this.api}/connections/suggestions?limit=${limit}`);
  }
  getMutualConnections(otherUserId: number, limit = 10): Observable<MutualConnectionDTO[]> {
    return this.http.get<MutualConnectionDTO[]>(`${this.api}/connections/mutuals/${otherUserId}?limit=${limit}`);
  }
  getMutualConnectionCount(otherUserId: number): Observable<number> {
    return this.http.get<number>(`${this.api}/connections/mutuals/${otherUserId}/count`);
  }

  sendMessage(receiverId: number, content: string): Observable<MessageDTO> {
    return this.http.post<MessageDTO>(`${this.api}/messages`, { receiverId, content });
  }
  getConversation(userId: number, page = 0): Observable<PageResponse<MessageDTO>> {
    return this.http.get<PageResponse<MessageDTO>>(`${this.api}/messages/${userId}?page=${page}`);
  }
  getConversations(): Observable<ConversationDTO[]> {
    return this.http.get<ConversationDTO[]>(`${this.api}/conversations`);
  }
  markAsRead(senderId: number): Observable<void> {
    return this.http.put<void>(`${this.api}/messages/read/${senderId}`, {});
  }
  sendMessageWithReply(receiverId: number, content: string, replyToId: number | null): Observable<MessageDTO> {
    const body: any = { receiverId, content };
    if (replyToId) body.replyToId = replyToId;
    return this.http.post<MessageDTO>(`${this.api}/messages`, body);
  }
  deleteMessage(messageId: number, deleteType: string): Observable<MessageDeleteDTO> {
    return this.http.post<MessageDeleteDTO>(`${this.api}/messages/delete`, { messageId, deleteType });
  }
  sendTyping(receiverId: number, typing: boolean): Observable<void> {
    return this.http.post<void>(`${this.api}/messages/typing`, { receiverId, typing });
  }
  toggleReaction(messageId: number, emoji: string): Observable<ReactionDTO> {
    return this.http.post<ReactionDTO>(`${this.api}/messages/react`, { messageId, emoji });
  }
  getReactions(messageId: number): Observable<ReactionDTO[]> {
    return this.http.get<ReactionDTO[]>(`${this.api}/messages/${messageId}/reactions`);
  }

  getNotifications(page = 0): Observable<PageResponse<NotificationDTO>> {
    return this.http.get<PageResponse<NotificationDTO>>(`${this.api}/notifications?page=${page}`);
  }
  getUnreadCount(): Observable<any> {
    return this.http.get(`${this.api}/notifications/unread-count`);
  }
  markNotificationsRead(): Observable<void> {
    return this.http.put<void>(`${this.api}/notifications/read`, {});
  }

  getEvents(page = 0, size = 10): Observable<PageResponse<EventDTO>> {
    return this.http.get<PageResponse<EventDTO>>(`${this.api}/events?page=${page}&size=${size}`);
  }
  getUpcomingEvents(page = 0, size = 10): Observable<PageResponse<EventDTO>> {
    return this.http.get<PageResponse<EventDTO>>(`${this.api}/events/upcoming?page=${page}&size=${size}`);
  }
  getPastEvents(page = 0, size = 10): Observable<PageResponse<EventDTO>> {
    return this.http.get<PageResponse<EventDTO>>(`${this.api}/events/past?page=${page}&size=${size}`);
  }
  getThisWeekEvents(page = 0, size = 10): Observable<PageResponse<EventDTO>> {
    return this.http.get<PageResponse<EventDTO>>(`${this.api}/events/this-week?page=${page}&size=${size}`);
  }
  getEventsByCategory(category: string, page = 0, size = 10): Observable<PageResponse<EventDTO>> {
    return this.http.get<PageResponse<EventDTO>>(`${this.api}/events/category/${category}?page=${page}&size=${size}`);
  }
  getMyEvents(page = 0, size = 10): Observable<PageResponse<EventDTO>> {
    return this.http.get<PageResponse<EventDTO>>(`${this.api}/events/mine?page=${page}&size=${size}`);
  }
  getEventById(id: number): Observable<EventDTO> {
    return this.http.get<EventDTO>(`${this.api}/events/${id}`);
  }
  createEvent(data: CreateEventRequest): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.api}/events`, data);
  }
  updateEvent(id: number, data: Partial<CreateEventRequest>): Observable<EventDTO> {
    return this.http.put<EventDTO>(`${this.api}/events/${id}`, data);
  }
  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/events/${id}`);
  }
  setRsvp(eventId: number, status: RsvpStatus): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.api}/events/${eventId}/rsvp`, { status });
  }
  removeRsvp(eventId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/events/${eventId}/rsvp`);
  }
  getEventAttendees(eventId: number, status: RsvpStatus = 'GOING'): Observable<EventAttendeeDTO[]> {
    return this.http.get<EventAttendeeDTO[]>(`${this.api}/events/${eventId}/attendees?status=${status}`);
  }
  downloadEventIcs(eventId: number): string {
    return `${this.api}/events/${eventId}/ics`;
  }
  getEventChatAccess(eventId: number): Observable<{ canAccess: boolean }> {
    return this.http.get<{ canAccess: boolean }>(`${this.api}/events/${eventId}/chat/access`);
  }
  getEventChatMessages(eventId: number, page = 0, size = 50): Observable<PageResponse<EventChatMessageDTO>> {
    return this.http.get<PageResponse<EventChatMessageDTO>>(`${this.api}/events/${eventId}/chat?page=${page}&size=${size}`);
  }
  sendEventChatMessage(eventId: number, content: string): Observable<EventChatMessageDTO> {
    return this.http.post<EventChatMessageDTO>(`${this.api}/events/${eventId}/chat`, { content });
  }

  getAllCommunities(page = 0): Observable<PageResponse<CommunityDTO>> {
    return this.http.get<PageResponse<CommunityDTO>>(`${this.api}/communities?page=${page}`);
  }
  getMyCommunities(page = 0): Observable<PageResponse<CommunityDTO>> {
    return this.http.get<PageResponse<CommunityDTO>>(`${this.api}/communities/my?page=${page}`);
  }
  searchCommunities(q: string, page = 0): Observable<PageResponse<CommunityDTO>> {
    return this.http.get<PageResponse<CommunityDTO>>(`${this.api}/communities/search?q=${q}&page=${page}`);
  }
  getCommunity(id: number): Observable<CommunityDTO> {
    return this.http.get<CommunityDTO>(`${this.api}/communities/${id}`);
  }
  createCommunity(data: any): Observable<CommunityDTO> {
    return this.http.post<CommunityDTO>(`${this.api}/communities`, data);
  }
  joinCommunity(id: number): Observable<CommunityDTO> {
    return this.http.post<CommunityDTO>(`${this.api}/communities/${id}/join`, {});
  }
  leaveCommunity(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/communities/${id}/leave`);
  }
  getCommunityMembers(id: number, page = 0): Observable<PageResponse<UserDTO>> {
    return this.http.get<PageResponse<UserDTO>>(`${this.api}/communities/${id}/members?page=${page}`);
  }
  getCommunityPosts(id: number, page = 0): Observable<PageResponse<CommunityPostDTO>> {
    return this.http.get<PageResponse<CommunityPostDTO>>(`${this.api}/communities/${id}/posts?page=${page}`);
  }
  getCommunityFeed(page = 0): Observable<PageResponse<CommunityPostDTO>> {
    return this.http.get<PageResponse<CommunityPostDTO>>(`${this.api}/communities/feed?page=${page}`);
  }
  createCommunityPost(communityId: number, data: any): Observable<CommunityPostDTO> {
    return this.http.post<CommunityPostDTO>(`${this.api}/communities/${communityId}/posts`, data);
  }
  getCommunityPost(postId: number): Observable<CommunityPostDTO> {
    return this.http.get<CommunityPostDTO>(`${this.api}/communities/posts/${postId}`);
  }
  voteCommunityPost(postId: number, value: number): Observable<any> {
    return this.http.post(`${this.api}/communities/posts/${postId}/vote`, { value });
  }
  getCommunityComments(postId: number, page = 0): Observable<PageResponse<CommunityCommentDTO>> {
    return this.http.get<PageResponse<CommunityCommentDTO>>(`${this.api}/communities/posts/${postId}/comments?page=${page}`);
  }
  addCommunityComment(postId: number, data: any): Observable<CommunityCommentDTO> {
    return this.http.post<CommunityCommentDTO>(`${this.api}/communities/posts/${postId}/comments`, data);
  }
  voteCommunityComment(commentId: number, value: number): Observable<any> {
    return this.http.post(`${this.api}/communities/comments/${commentId}/vote`, { value });
  }
  inviteToCommunity(communityId: number, receiverId: number): Observable<any> {
    return this.http.post(`${this.api}/communities/${communityId}/invite`, { receiverId });
  }
  updateCommunity(id: number, data: any): Observable<CommunityDTO> {
    return this.http.put<CommunityDTO>(`${this.api}/communities/${id}`, data);
  }
  deleteCommunity(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/communities/${id}`);
  }
  uploadCommunityIcon(id: number, file: File): Observable<CommunityDTO> {
    const fd = new FormData(); fd.append('file', file);
    return this.http.post<CommunityDTO>(`${this.api}/communities/${id}/icon`, fd);
  }
  uploadCommunityBanner(id: number, file: File): Observable<CommunityDTO> {
    const fd = new FormData(); fd.append('file', file);
    return this.http.post<CommunityDTO>(`${this.api}/communities/${id}/banner`, fd);
  }
  updateMemberRole(communityId: number, userId: number, role: string): Observable<any> {
    return this.http.put(`${this.api}/communities/${communityId}/members/${userId}/role`, { role });
  }
  removeMember(communityId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/communities/${communityId}/members/${userId}`);
  }
  deleteCommunityPost(postId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/communities/posts/${postId}`);
  }

  acceptAnswer(postId: number, commentId: number): Observable<CommunityPostDTO> {
    return this.http.post<CommunityPostDTO>(`${this.api}/communities/posts/${postId}/accept-answer/${commentId}`, {});
  }
  unacceptAnswer(postId: number): Observable<CommunityPostDTO> {
    return this.http.delete<CommunityPostDTO>(`${this.api}/communities/posts/${postId}/accept-answer`);
  }
  unmaskAnonymous(postId: number): Observable<CommunityPostDTO> {
    return this.http.post<CommunityPostDTO>(`${this.api}/communities/posts/${postId}/unmask`, {});
  }

  getCommunityResources(communityId: number, q?: string, page = 0, size = 20): Observable<PageResponse<CommunityResourceDTO>> {
    let url = `${this.api}/communities/${communityId}/resources?page=${page}&size=${size}`;
    if (q) url += `&q=${encodeURIComponent(q)}`;
    return this.http.get<PageResponse<CommunityResourceDTO>>(url);
  }
  addCommunityResource(communityId: number, data: CreateResourceRequest): Observable<CommunityResourceDTO> {
    return this.http.post<CommunityResourceDTO>(`${this.api}/communities/${communityId}/resources`, data);
  }
  deleteCommunityResource(communityId: number, resourceId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/communities/${communityId}/resources/${resourceId}`);
  }
  trackResourceClick(communityId: number, resourceId: number): Observable<CommunityResourceDTO> {
    return this.http.post<CommunityResourceDTO>(`${this.api}/communities/${communityId}/resources/${resourceId}/click`, {});
  }

  toggleBookmark(postId: number): Observable<{ bookmarked: boolean }> {
    return this.http.post<{ bookmarked: boolean }>(`${this.api}/bookmarks/${postId}`, {});
  }
  getMyBookmarks(page = 0, size = 10): Observable<PageResponse<PostDTO>> {
    return this.http.get<PageResponse<PostDTO>>(`${this.api}/bookmarks?page=${page}&size=${size}`);
  }
  getBookmarkCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.api}/bookmarks/count`);
  }

  getTrendingHashtags(limit = 8): Observable<HashtagDTO[]> {
    return this.http.get<HashtagDTO[]>(`${this.api}/hashtags/trending?limit=${limit}`);
  }
  searchHashtags(q: string, limit = 10): Observable<HashtagDTO[]> {
    return this.http.get<HashtagDTO[]>(`${this.api}/hashtags/search?q=${q}&limit=${limit}`);
  }
  getPostsByHashtag(tag: string): Observable<PostDTO[]> {
    return this.http.get<PostDTO[]>(`${this.api}/hashtags/${tag}/posts`);
  }

  submitReport(req: ReportRequest): Observable<ReportDTO> {
    return this.http.post<ReportDTO>(`${this.api}/reports`, req);
  }
  blockUser(id: number): Observable<any> {
    return this.http.post(`${this.api}/users/${id}/block`, {});
  }
  unblockUser(id: number): Observable<any> {
    return this.http.delete(`${this.api}/users/${id}/block`);
  }
  getBlockStatus(id: number): Observable<{ blocked: boolean }> {
    return this.http.get<{ blocked: boolean }>(`${this.api}/users/${id}/block-status`);
  }
  getPendingReports(page = 0): Observable<PageResponse<ReportDTO>> {
    return this.http.get<PageResponse<ReportDTO>>(`${this.api}/admin/reports?page=${page}`);
  }
  resolveReport(id: number, status: string): Observable<void> {
    return this.http.put<void>(`${this.api}/admin/reports/${id}?status=${status}`, {});
  }

  universalSearch(q: string): Observable<SearchResultDTO> {
    return this.http.get<SearchResultDTO>(`${this.api}/search?q=${encodeURIComponent(q)}`);
  }

  createPoll(postId: number, data: CreatePollRequest): Observable<PollDTO> {
    return this.http.post<PollDTO>(`${this.api}/polls/posts/${postId}`, data);
  }
  getPollForPost(postId: number): Observable<PollDTO> {
    return this.http.get<PollDTO>(`${this.api}/polls/posts/${postId}`);
  }
  votePoll(pollId: number, optionId: number): Observable<PollDTO> {
    return this.http.post<PollDTO>(`${this.api}/polls/${pollId}/vote/${optionId}`, {});
  }

  giveKudos(req: CreateKudosRequest): Observable<KudosDTO> {
    return this.http.post<KudosDTO>(`${this.api}/kudos`, req);
  }
  getKudosReceived(userId: number, page = 0): Observable<PageResponse<KudosDTO>> {
    return this.http.get<PageResponse<KudosDTO>>(`${this.api}/kudos/received/${userId}?page=${page}`);
  }
  getKudosGiven(userId: number, page = 0): Observable<PageResponse<KudosDTO>> {
    return this.http.get<PageResponse<KudosDTO>>(`${this.api}/kudos/given/${userId}?page=${page}`);
  }
  getRecentKudos(page = 0): Observable<PageResponse<KudosDTO>> {
    return this.http.get<PageResponse<KudosDTO>>(`${this.api}/kudos/recent?page=${page}`);
  }
  getKudosStats(userId: number): Observable<{ received: number; given: number }> {
    return this.http.get<{ received: number; given: number }>(`${this.api}/kudos/stats/${userId}`);
  }

  getMyAchievements(): Observable<AchievementDTO[]> {
    return this.http.get<AchievementDTO[]>(`${this.api}/achievements/me`);
  }
  getAchievementsForUser(userId: number): Observable<AchievementDTO[]> {
    return this.http.get<AchievementDTO[]>(`${this.api}/achievements/user/${userId}`);
  }
  getAchievementStats(userId: number): Observable<AchievementStats> {
    return this.http.get<AchievementStats>(`${this.api}/achievements/stats/${userId}`);
  }
  checkInStreak(): Observable<StreakDTO> {
    return this.http.post<StreakDTO>(`${this.api}/streak/check-in`, {});
  }
  getMyStreak(): Observable<StreakDTO> {
    return this.http.get<StreakDTO>(`${this.api}/streak/me`);
  }

  createStory(req: CreateStoryRequest): Observable<StoryDTO> {
    return this.http.post<StoryDTO>(`${this.api}/stories`, req);
  }
  getStoriesFeed(): Observable<UserStoriesGroupDTO[]> {
    return this.http.get<UserStoriesGroupDTO[]>(`${this.api}/stories/feed`);
  }
  markStoryViewed(storyId: number): Observable<void> {
    return this.http.post<void>(`${this.api}/stories/${storyId}/view`, {});
  }
  deleteStory(storyId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/stories/${storyId}`);
  }
  getStoryViewers(storyId: number): Observable<StoryViewer[]> {
    return this.http.get<StoryViewer[]>(`${this.api}/stories/${storyId}/viewers`);
  }

  createEndorsement(data: CreateEndorsementRequest): Observable<SkillEndorsementDTO> {
    return this.http.post<SkillEndorsementDTO>(`${this.api}/endorsements`, data);
  }
  removeEndorsement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/endorsements/${id}`);
  }
  getEndorsementsForUser(userId: number, page = 0): Observable<PageResponse<SkillEndorsementDTO>> {
    return this.http.get<PageResponse<SkillEndorsementDTO>>(`${this.api}/endorsements/user/${userId}?page=${page}`);
  }
  getMyEndorsementsForUser(userId: number, endorserId: number): Observable<SkillEndorsementDTO[]> {
    return this.http.get<SkillEndorsementDTO[]>(`${this.api}/endorsements/user/${userId}/by/${endorserId}`);
  }
  getEndorsersForSkill(userId: number, skill: string): Observable<SkillEndorsementDTO[]> {
    return this.http.get<SkillEndorsementDTO[]>(`${this.api}/endorsements/user/${userId}/skill/${encodeURIComponent(skill)}`);
  }
  getEndorsementSummary(userId: number): Observable<EndorsementSummaryDTO> {
    return this.http.get<EndorsementSummaryDTO>(`${this.api}/endorsements/user/${userId}/summary`);
  }

  getMyProfileCompletion(): Observable<ProfileCompletionDTO> {
    return this.http.get<ProfileCompletionDTO>(`${this.api}/career/completion/me`);
  }
  getProfileCompletion(userId: number): Observable<ProfileCompletionDTO> {
    return this.http.get<ProfileCompletionDTO>(`${this.api}/career/completion/user/${userId}`);
  }
  searchUsersBySkill(skill: string, page = 0): Observable<PageResponse<UserDTO>> {
    return this.http.get<PageResponse<UserDTO>>(`${this.api}/career/skills/search?skill=${encodeURIComponent(skill)}&page=${page}`);
  }
  autocompleteSkills(q: string, limit = 10): Observable<SkillSuggestion[]> {
    return this.http.get<SkillSuggestion[]>(`${this.api}/career/skills/autocomplete?q=${encodeURIComponent(q)}&limit=${limit}`);
  }
  getTrendingSkills(limit = 10): Observable<SkillSuggestion[]> {
    return this.http.get<SkillSuggestion[]>(`${this.api}/career/skills/trending?limit=${limit}`);
  }
}
