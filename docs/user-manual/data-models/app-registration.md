# AppRegistration

Stores user registration information for the application. Uses a singleton pattern with a fixed ID.

**Database Table**: `app_registration`

## Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Primary key, always "REGISTRATION" |
| `installId` | String | Unique installation UUID |
| `email` | String | User email address |
| `displayName` | String | User display name |
| `registeredAt` | Instant | Registration timestamp |
| `lastReportAt` | Instant | Last diagnostic report timestamp |

## Report Defaults

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `defaultIncludeSystemInfo` | boolean | true | Include system info in reports |
| `defaultIncludeLogs` | boolean | true | Include logs in reports |
| `defaultIncludeScreenshot` | boolean | false | Include screenshot in reports |

## Singleton Pattern

This entity uses a singleton pattern - there is always exactly one record with `id = "REGISTRATION"`. This stores the user's registration information for the installation.

## Usage

The registration information is used for:
- Identifying the installation
- Pre-filling diagnostic report forms
- Tracking user preferences for report content
