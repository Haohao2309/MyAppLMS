# Community Feature - Backend Sync Guide

This document summarizes the frontend (Android) implementation to help the Backend team align their APIs and Database structure.

## 1. API Contracts (Base URL: `/api/community/`)

| Endpoint | Method | Request Body | Response DTO | Description |
| :--- | :--- | :--- | :--- | :--- |
| `posts` | GET | Queries: `category`, `page`, `size` | `List<PostResponse>` | Paginated post list |
| `posts/{id}` | GET | - | `PostDetailResponse` | Full content + `comments[]` + `likedByMe` |
| `posts` | POST | `CreatePostRequest` | `PostResponse` | Create new post |
| `posts/{id}` | DELETE | - | `CommunityActionResponse` | Delete post (Owner/Admin) |
| `posts/{id}/like`| POST | - | `CommunityActionResponse` | Toggle like status |
| `posts/{id}/comments` | POST | `CreateCommentRequest` | `CommentResponse` | Add comment |
| `posts/{id}/comments/{cid}` | DELETE | - | `CommunityActionResponse` | Delete comment (Owner/Admin) |

## 2. Key DTO Structures

### PostResponse (Core Model)
```json
{
  "id": "6a05feb8aa...", // MongoDB ObjectId
  "title": "String",
  "content": "String",
  "category": "String",
  "type": "String",
  "authorName": "String",
  "authorRole": "TEACHER | STUDENT | ADMIN",
  "userId": "String", // To check ownership on FE
  "views": 0,
  "likes": 0,
  "commentsCount": 0,
  "createdAt": "2024-05-14T10:00:00.000Z" // ISO Format required
}
```

### CommunityActionResponse (Unified for actions)
```json
{
  "success": true,
  "liked": true,
  "likesCount": 57,
  "message": "Success message"
}
```

## 3. Important Notes for Backend
1. **ObjectId**: The `{id}` in paths must always be the post's unique identifier, not the userId.
2. **Date Format**: Please return `createdAt` in **ISO 8601** format. The FE uses `TimeUtils` to convert this to relative time (e.g., "5 mins ago").
3. **Roles**: `authorRole` is used to display colorful badges. Please ensure this is returned for every post/comment author.
4. **Ownership**: `userId` is critical for the FE to show the "Tác giả" (Author) badge and "Delete/Edit" options.
5. **Like Status**: `PostDetailResponse` **MUST** include `likedByMe: boolean` based on the current authenticated user.
