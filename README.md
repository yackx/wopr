# WOPR
                 
**Thermonuclear war simulation reminiscent of an eighties movie**

<img width="1252" alt="world" src="https://github.com/user-attachments/assets/69ecb4ec-3770-46b3-8181-b8302f83a3fe" />
    
## At a glance

This toy simulation is a proof-of-concept but it is already playable.

You automatically log in to WOPR using your terminal. Several strategy games are listed in the menu although only one is implemented at this stage.

The thermonuclear war gameplay focuses narrowly on direct casualties. There are a few scenarios available involving countries with nuclear capabilities. You choose one and you let the simulation run its course. Nukes are launched from predetermined launch sites (facilities, submarine or mobile).

⬇️ Download the latest build from the releases section.
        
## Features

- Built-in retro themes
- Terminal-based
- Basic casualties simulation

## Stack

- Java 22
- [libGDX](https://libgdx.com/)
- [Gradle](https://gradle.org/)

## Run

Install the stack and execute `gradle.bat run` or `./gradlew run` from the command line, or launch the `run` task from your IDE (wopr - Tasks - application).
See [gradle.md]() for more information.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3.

Desktop is the only supported platform. 

## Contribute

Feel free to reach out in the discussion section if you want to contribute (bug fixes, enhancements or new features). Please do not open an issue for request features.

The code leverages functional programming. Although it may be unwise in for resource-intensive games, its cost-benefit ratio seems favorable for a 2D casual simulation.

## License

[EUPL - European Union Public License version 1.2](LICENSE.txt)

The EUPL is the European Free/Open Source Software (F/OSS) licence. It is compatible with GLP-3.0 and available in 23 languages. [More info](https://interoperable-europe.ec.europa.eu/collection/eupl/introduction-eupl-licence)
              
## Credits

- [Terminal sound](https://www.youtube.com/watch?v=DP-_QNCx51s) by [soundeffects7746](www.youtube.com/@soundeffects7746)
- Launch sound borrowed from [strider alarm](https://www.youtube.com/watch?v=S8-67-zGZUE) in Half-Life2
- [3270 Nerd Font](https://www.nerdfonts.com/font-downloads) derived from IBM 3270

## Samples

No winner

<img width="1498" alt="no_winner" src="https://github.com/user-attachments/assets/1dbb9537-cf20-4033-b924-fe166d3ec7a4" />

Several themes included (Pascal theme)

<img width="1513" alt="pascal" src="https://github.com/user-attachments/assets/1d5660f5-ada8-42d9-a0d7-a46f43b83bd1" />

