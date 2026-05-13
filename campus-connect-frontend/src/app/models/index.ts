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
  birthday: string | null;
  workAnniversary: string | null;
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

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  last: boolean;
  number: number;
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
  bookmarkedByMe: boolean;
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
  message: string | null;
  sentByMe: boolean;
  createdAt: string;
}

export interface ConnectionSuggestionDTO {
  userId: number;
  name: string;
  profilePicUrl: string | null;
  position: string | null;
  department: string | null;
  role: string;
  mutualCount: number;
  reason: string;
  score: number;
}

export interface MutualConnectionDTO {
  userId: number;
  name: string;
  profilePicUrl: string | null;
  position: string | null;
  department: string | null;
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

export interface HashtagDTO {
  id: number;
  tag: string;
  usageCount: number;
}

export interface ReportRequest {
  targetType: 'POST' | 'COMMENT' | 'USER' | 'COMMUNITY_POST';
  targetId: number;
  reason: string;
  details?: string;
}

export interface ReportDTO {
  id: number;
  reporterId: number;
  reporterName: string;
  targetType: string;
  targetId: number;
  reason: string;
  details: string;
  status: string;
  createdAt: string;
}

export interface SearchResultDTO {
  users: UserDTO[];
  posts: PostDTO[];
  communities: CommunityDTO[];
  events: EventDTO[];
  hashtags: HashtagDTO[];
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

export type CommunityPostType = 'DISCUSSION' | 'QUESTION';

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
  postType: CommunityPostType;
  anonymous: boolean;
  unmaskedByMe: boolean;
  acceptedAnswerId: number | null;
  resolved: boolean;
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
  isAcceptedAnswer: boolean;
  showReplyInput?: boolean;
  replyContent?: string;
  showReplies?: boolean;
}

export type ResourceType = 'LINK' | 'FILE';

export interface CommunityResourceDTO {
  id: number;
  communityId: number;
  uploadedById: number;
  uploadedByName: string;
  uploadedByProfilePic: string | null;
  title: string;
  description: string | null;
  resourceType: ResourceType;
  url: string;
  fileSizeBytes: number | null;
  mimeType: string | null;
  tags: string | null;
  clickCount: number;
  createdAt: string;
}

export interface CreateResourceRequest {
  title: string;
  description?: string;
  resourceType: ResourceType;
  url: string;
  fileSizeBytes?: number;
  mimeType?: string;
  tags?: string;
}

export interface PollOptionDTO {
  id: number;
  optionText: string;
  voteCount: number;
  percentage: number;
}

export interface PollDTO {
  id: number;
  postId: number;
  question: string;
  multiChoice: boolean;
  expiresAt: string | null;
  expired: boolean;
  totalVotes: number;
  options: PollOptionDTO[];
  hasVoted: boolean;
  myVotedOptionIds: number[];
}

export interface CreatePollRequest {
  question: string;
  options: string[];
  multiChoice: boolean;
  expiresAt?: string | null;
}

export interface KudosDTO {
  id: number;
  giverId: number;
  giverName: string;
  giverProfilePic: string;
  giverPosition: string;
  receiverId: number;
  receiverName: string;
  receiverProfilePic: string;
  receiverPosition: string;
  category: string;
  message: string;
  isPublic: boolean;
  createdAt: string;
}

export interface CreateKudosRequest {
  receiverId: number;
  category: string;
  message?: string;
  isPublic?: boolean;
}

export interface AchievementDTO {
  id: number;
  code: string;
  name: string;
  description: string;
  icon: string;
  tier: string;
  points: number;
  earnedAt: string | null;
  earned: boolean;
}

export interface StreakDTO {
  currentStreak: number;
  longestStreak: number;
  lastActiveDate: string | null;
  totalCheckIns: number;
  checkedInToday: boolean;
}

export interface AchievementStats {
  earnedCount: number;
  totalAvailable: number;
  totalPoints: number;
}

export interface StoryDTO {
  id: number;
  userId: number;
  userName: string;
  userProfilePic: string;
  mediaUrl: string | null;
  caption: string | null;
  backgroundColor: string | null;
  createdAt: string;
  expiresAt: string;
  viewCount: number;
  viewedByMe: boolean;
}

export interface UserStoriesGroupDTO {
  userId: number;
  userName: string;
  userProfilePic: string;
  allViewed: boolean;
  stories: StoryDTO[];
}

export interface CreateStoryRequest {
  mediaUrl?: string;
  caption?: string;
  backgroundColor?: string;
}

export interface StoryViewer {
  userId: number;
  userName: string;
  userProfilePic: string;
  viewedAt: string;
}

export type RsvpStatus = 'GOING' | 'INTERESTED' | 'NOT_GOING';
export type EventType = 'PHYSICAL' | 'VIRTUAL' | 'HYBRID';
export type EventCategory =
  'WORKSHOP' | 'WEBINAR' | 'HACKATHON' | 'NETWORKING' |
  'TRAINING' | 'SOCIAL' | 'CONFERENCE' | 'OTHER';

export interface EventDTO {
  id: number;
  title: string;
  description: string;
  eventDate: string;
  eventEndDate: string | null;
  location: string | null;
  imageUrl: string | null;
  coverUrl: string | null;
  eventType: EventType;
  category: EventCategory;
  virtualLink: string | null;
  maxParticipants: number | null;
  showAttendees: boolean;
  createdById: number;
  createdByName: string;
  createdByProfilePic: string | null;
  createdByRole: string;
  createdAt: string;
  goingCount: number;
  interestedCount: number;
  myRsvpStatus: RsvpStatus | null;
  isPast: boolean;
  isLive: boolean;
}

export interface CreateEventRequest {
  title: string;
  description?: string;
  eventDate: string;
  eventEndDate?: string;
  location?: string;
  imageUrl?: string;
  coverUrl?: string;
  eventType?: EventType;
  category?: EventCategory;
  virtualLink?: string;
  maxParticipants?: number;
  showAttendees?: boolean;
}

export interface EventAttendeeDTO {
  userId: number;
  name: string;
  profilePicUrl: string | null;
  position: string | null;
  department: string | null;
  role: string;
  status: RsvpStatus;
  rsvpAt: string;
}

export interface EventChatMessageDTO {
  id: number;
  eventId: number;
  userId: number;
  userName: string;
  userProfilePic: string | null;
  userRole: string;
  content: string;
  createdAt: string;
}

export type EndorsementCategory =
  'TECHNICAL' | 'LEADERSHIP' | 'TEAMWORK' |
  'COMMUNICATION' | 'PROBLEM_SOLVING' | 'MENTORSHIP';

export interface SkillEndorsementDTO {
  id: number;
  endorserId: number;
  endorserName: string;
  endorserProfilePic: string | null;
  endorserPosition: string | null;
  endorseeId: number;
  endorseeName: string;
  skill: string | null;
  category: EndorsementCategory | null;
  message: string | null;
  createdAt: string;
}

export interface CreateEndorsementRequest {
  endorseeId: number;
  skill?: string;
  category?: EndorsementCategory;
  message?: string;
}

export interface SkillCount {
  label: string;
  count: number;
}

export interface EndorsementSummaryDTO {
  totalReceived: number;
  totalGiven: number;
  topSkills: SkillCount[];
  categoryBreakdown: SkillCount[];
}

export interface MissingField {
  key: string;
  label: string;
  weight: number;
}

export interface ProfileCompletionDTO {
  percent: number;
  missing: MissingField[];
}

export interface SkillSuggestion {
  skill: string;
  count: number;
}
