# WearNOW Android

![wearnow-screenshot](/assets/wearnow_screenshot.png?raw=true)

This is the repository for the WearNOW Android application.

WearNOW allows users to virtually try on clothing using their own photos. It leverages generative AI through Amazon Bedrock to create a realistic virtual fitting room experience, helping users visualize how clothes will look on them before making a purchase.

## Core Features

- Virtual Try-On: Use a live camera feed or a photo from the gallery to try on different garments.
- Photo Library: Save personal photos within the app for quick access in future sessions.
- Style Customization: Select and adjust various garment types and styles.
- Try-On History: Review past virtual try-on sessions.

## Architecture and Technology

![wearnow-architecture](/assets/wearnow_architecture.png?raw=true)

The WearNOW Android application connects to a serverless backend built on AWS and managed by AWS Amplify. The frontend is a native Android application (Java/Kotlin).

The backend stack includes:

- AWS Amplify as the core framework.
- Amazon Cognito for user authentication.
- Amazon S3 for storing user and garment images.
- AWS AppSync for the GraphQL API.
- AWS Lambda for business logic.
- Amazon Bedrock for the generative AI-powered virtual try-on functionality with Amazon Nova Canvas.
- Amazon DynamoDB for data storage.

## Backend Repository

The source code for the AWS Amplify backend is maintained in a separate repository. You can find it [here](https://github.com/harissabil/wearnow-backend)

## Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- An Android device or emulator

### Installation & Setup

1. Clone or download this repository.
2. Open the project in Android Studio.
3. The project requires configuration files (amplify_outputs.json) from the AWS Amplify backend to connect to the cloud resources. These files must be generated from the backend repository and placed in the appropriate directory in the Android project.
4. Build and run the application on an emulator or a physical device.