PicturePuzzle
=============

Android puzzle game inspired by the classic 15-puzzle game that breaks up a picture into blocks and lets the user swap two blocks at a time to unscramble the picture. But unlike the classic game which swaps two adjacent blocks, this game lets swap any two blocks at a time.

How It Works
==============
The game uses a 3×4 grid of rectangular Imageview blocks as the main layout. At the beginning of the game, a picture is loaded, and broken up into 3×4 rectangular pieces as bitmaps. These bitmaps are then positioned randomly into the Imageviews, creating the initial view of the game.

Each of these bitmaps is contained within an ImageView, which have touch and drag listeners attached to it. The drag listeners keep track of the position of the ImageViews and at the end of each drag, compare the current position of the group of ImageViews with their respective original positions. When all the ImageViews are placed into their original positions, the puzzle is solved.

The user can drag these individual bitmap pieces around to swap them among each other. Of course the aim is to finish unscrambling the picture as quickly as possible. The main activity class of the game is contained in the class DragDropActivity.
