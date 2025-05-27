# Object detection and assistance program for visually impaired users

## About
An Android application designed to assist visually impaired users by providing real-time
object detection through the device camera, with spoken feedback using Text-to-Speech.

## Features
- Real-time object detection using TensorFlow Lite
- Audio feedback via TextToSpeech to announce detected objects
- Visual overlay showing bounding boxes around detected items
- Accessible "Stop app" button for exit
- Filters to avoid repeated announcements

## Technologies used
- Android (Java, API 21+)
- CameraX for live camera feed
- TensorFlow Lite (EfficientDet-Lite0) for object detection
- TextToSpeech API for voice feedback

## Setup and installation
1. Clone or download this repository.
2. Open it in Android Studio.
3. Check the following are present in the "assets" folder:
    - "efficientdet_lite0.tflite" – the trained model
    - "labelmap.txt" – labels corresponding to the model output
4. Connect your Android device via USB.
5. Build and run the app.

## Project structure
com.miroslava958.objectdetectionandassistance/
- MainActivity.java - entry point of the app
- ObjectDetector.java - Handles image analysis and model inference
- OverlayView.java - Custom view for drawing bounding boxes
- TextToSpeechManager.java - Handles TTS functionality
- ImageUtils.java - Converts camera images to Bitmap
- DetectionResult.java - Model for a single detection result

## How it works
- The camera feed is analysed frame-by-frame.
- Objects with a confidence score above a threshold are detected.
- Labels are spoken if not previously announced.
- Bounding boxes are drawn over the camera preview for visual feedback.
- Users can exit the app using the on-screen stop button.

## Permissions
- CAMERA - for real-time object detection

## Author
- Miroslava Milcheva 
- BSc Computing - Final Year Project

## License
This project is for academic purposes and not intended for commercial distribution.

## Known issues and limitations
- Detection accuracy may vary depending on lighting, camera quality and the model functionality.
- Voice control was implemented but later removed as did not work correctly.

## Future Improvements
- Add voice command support for easier work with the app
- Include object tracking across frames
- Integrate facial recognition using local gallery comparison
- Create distance detection by including a feature to alert users when objects are within a certain distance.
- Develop a directional guidance system to navigate users through their daily surroundings
- Extend the usability of the app to non-English speaking users.

## Acknowledgements
- TensorFlow Lite team for EfficientDet-Lite0 model
- Android CameraX library contributors
- I would like to thank my supervisor and the BSc Computing Department at Birkbeck for their guidance and support throughout this project.
- I also appreciate the encouragement and feedback from my peers and family.
