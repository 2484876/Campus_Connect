import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  PostDTO, CommentDTO, UserDTO, ConnectionDTO,
  MessageDTO, ConversationDTO, NotificationDTO,
  EventDTO, PageResponse, CommunityDTO, CommunityPostDTO, CommunityCommentDTO,
  ExperienceDTO, MessageDeleteDTO, ReactionDTO
} from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {

  private api = environment.apiUrl;

  constructor(private http: HttpClient) { }

  uploadImage(file: File): Observable<{ url: string }> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<{ url: string }>(`${this.api}/upload/image`, fd);
  }

  uploadVideo(file: File): Observable<{ url: string }> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<{ url: string }>(`${this.api}/upload/video`, fd);
  }

  uploadAvatar(file: File): Observable<{ url: string }> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<{ url: string }>(`${this.api}/upload/avatar`, fd);
  }

  updateAvatar(file: File): Observable<UserDTO> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<UserDTO>(`${this.api}/users/me/avatar`, fd);
  }

  getFeed(page: number, size = 10): Observable<PageResponse<PostDTO>> {
    return this.http.get<PageResponse<PostDTO>>(`${this.api}/feed?page=${page}&size=${size}`);
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

  getConnections(page = 0): Observable<PageResponse<ConnectionDTO>> {
    return this.http.get<PageResponse<ConnectionDTO>>(`${this.api}/connections?page=${page}`);
  }

  getPendingRequests(page = 0): Observable<PageResponse<ConnectionDTO>> {
    return this.http.get<PageResponse<ConnectionDTO>>(`${this.api}/connections/pending?page=${page}`);
  }

  sendConnectionRequest(receiverId: number): Observable<ConnectionDTO> {
    return this.http.post<ConnectionDTO>(`${this.api}/connections/request`, { receiverId });
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

  getNotifications(page = 0): Observable<PageResponse<NotificationDTO>> {
    return this.http.get<PageResponse<NotificationDTO>>(`${this.api}/notifications?page=${page}`);
  }

  getUnreadCount(): Observable<any> {
    return this.http.get(`${this.api}/notifications/unread-count`);
  }

  markNotificationsRead(): Observable<void> {
    return this.http.put<void>(`${this.api}/notifications/read`, {});
  }

  getEvents(page = 0): Observable<PageResponse<EventDTO>> {
    return this.http.get<PageResponse<EventDTO>>(`${this.api}/events?page=${page}`);
  }

  getUpcomingEvents(page = 0): Observable<PageResponse<EventDTO>> {
    return this.http.get<PageResponse<EventDTO>>(`${this.api}/events/upcoming?page=${page}`);
  }

  createEvent(data: any): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.api}/events`, data);
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
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<CommunityDTO>(`${this.api}/communities/${id}/icon`, fd);
  }

  uploadCommunityBanner(id: number, file: File): Observable<CommunityDTO> {
    const fd = new FormData();
    fd.append('file', file);
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

  uploadUserBanner(file: File): Observable<UserDTO> {
    const fd = new FormData();
    fd.append('file', file);
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
}
