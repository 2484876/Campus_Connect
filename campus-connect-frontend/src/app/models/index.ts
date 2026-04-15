export interface AuthResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: string;
}

export interface UserDTO {
  id: number;
  name: string;
  email: string;
  role: string;
  department: string;
  position: string;
  bio: string;
  profilePicUrl: string;
  phone: string;
  createdAt: string;
  skills: string[];
  connectionCount: number;
  connectionStatus: string;
  bannerUrl: string;
  experiences: ExperienceDTO[];
}

export interface PostDTO {
  id: number;
  content: string;
  imageUrl: string;
  videoUrl: string;
  postType: string;
  createdAt: string;
  authorId: number;
  authorName: string;
  authorProfilePic: string;
  authorPosition: string;
  authorRole: string;
  authorDepartment: string;
  likeCount: number;
  commentCount: number;
  likedByMe: boolean;
  recentComments: CommentDTO[];
  showComments?: boolean;
  newComment?: string;
}

export interface CommentDTO {
  id: number;
  content: string;
  authorId: number;
  authorName: string;
  authorProfilePic: string;
  createdAt: string;
}

export interface ConnectionDTO {
  connectionId: number;
  userId: number;
  userName: string;
  userProfilePic: string;
  userPosition: string;
  userRole: string;
  userDepartment: string;
  status: string;
  createdAt: string;
}

export interface NotificationDTO {
  id: number;
  type: string;
  actorId: number;
  actorName: string;
  actorProfilePic: string;
  referenceId: number;
  isRead: boolean;
  createdAt: string;
  message: string;
}

export interface EventDTO {
  id: number;
  title: string;
  description: string;
  eventDate: string;
  location: string;
  imageUrl: string;
  maxParticipants: number;
  createdById: number;
  createdByName: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  last: boolean;
  number: number;
}

export interface CommunityDTO {
  id: number;
  name: string;
  description: string;
  bannerUrl: string;
  iconUrl: string;
  createdById: number;
  createdByName: string;
  isPrivate: boolean;
  memberCount: number;
  isMember: boolean;
  memberRole: string;
  createdAt: string;
}

export interface CommunityPostDTO {
  id: number;
  communityId: number;
  communityName: string;
  communityIconUrl: string;
  authorId: number;
  authorName: string;
  authorProfilePic: string;
  authorRole: string;
  content: string;
  imageUrl: string;
  videoUrl: string;
  upvotes: number;
  downvotes: number;
  score: number;
  userVote: number;
  commentCount: number;
  createdAt: string;
}

export interface CommunityCommentDTO {
  id: number;
  postId: number;
  authorId: number;
  authorName: string;
  authorProfilePic: string;
  parentCommentId: number;
  content: string;
  upvotes: number;
  downvotes: number;
  score: number;
  userVote: number;
  replyCount: number;
  replies: CommunityCommentDTO[];
  createdAt: string;
  showReplyInput?: boolean;
  replyContent?: string;
  showReplies?: boolean;
}

export interface ExperienceDTO {
  id: number;
  title: string;
  employmentType: string;
  company: string;
  location: string;
  startDate: string;
  endDate: string;
  isCurrent: boolean;
  description: string;
}

export interface ReactionDTO {
  id: number;
  messageId: number;
  userId: number;
  userName: string;
  emoji: string;
  createdAt: string;
}

export interface ReactionNotificationDTO {
  messageId: number;
  userId: number;
  userName: string;
  emoji: string;
  action: string;
}

export interface MessageDTO {
  id: number;
  senderId: number;
  senderName: string;
  senderProfilePic: string;
  receiverId: number;
  content: string;
  readStatus: boolean;
  readAt: string | null;
  deleted: boolean;
  deletedBy: number | null;
  deleteType: string | null;
  hiddenFor: number | null;
  replyToId: number | null;
  replyToContent: string | null;
  replyToSenderName: string | null;
  reactions: ReactionDTO[];
  createdAt: string;
}

export interface ConversationDTO {
  userId: number;
  userName: string;
  userProfilePic: string;
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
}

export interface ReadReceiptDTO {
  readByUserId: number;
  senderUserId: number;
  messageIds: number[];
  readAt: string;
}

export interface TypingDTO {
  userId: number;
  userName: string;
  typing: boolean;
}

export interface MessageDeleteDTO {
  messageId: number;
  deletedBy: number;
  otherUserId: number;
  deleteType: string;
}
