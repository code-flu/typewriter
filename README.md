# Typewriter

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=flat)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25.0.2-blue?style=flat)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat)](LICENSE)

A powerful JavaFX application that types code with custom line priority ordering. Perfect for typewriter animations in videos and presentations.

## 🌟 Features

- **Syntax Highlighting**: Java, Python, C#, JavaScript, HTML, XML, SQL, C++
- **Line Priority Control**: Drag-and-drop to reorder typing sequence
- **Adjustable Speed**: 10-1000ms per character
- **Real-time Editor**: Line numbers, code folding, syntax highlighting
- **Dual Mode**: Edit mode and reorder mode
- **Cross-platform**: Windows, macOS, Linux (Java 17+)

## 📋 Requirements

- Java 17+
- JavaFX 25.0.2 (included via Maven)
- Maven 3.8+

## 🚀 Quick Start

### Build
```bash
git clone https://github.com/code-flu/typewriter.git
cd typewriter
mvn clean package
```

### Run
```bash
# Option 1: Maven
mvn javafx:run

# Option 2: JAR
java -jar target/typewriter-1.0-SNAPSHOT.jar

# Option 3: IDE
Run com.codeflu.typewriter.Launcher
```

## 📖 Usage

1. **Write Code** - Paste/type code in editor, select language
2. **Reorder Mode** - Click "🔄 Reorder Mode" to enter line reordering
3. **Arrange Priority** - Drag lines with grip icon (⠿) to set typing order
4. **Configure Speed** - Use spinner (10-1000ms per character)
5. **Start** - Click "▶ Start Simulation" to begin typing
6. **Stop** - Click "⏹ Stop" to halt anytime

## 🎯 Use Cases

- Tutorial video typewriter effects
- Live coding demonstrations
- Educational presentations
- Screen recordings automation
- Technical demo sequences

## 🛠️ Architecture

- **Controller**: TypeWriterController.java - UI & simulation logic
- **Model**: ReorderLine.java - Data model for lines
- **Threading**: ScheduledExecutorService for non-blocking typing
- **Editor**: RSyntaxTextArea embedded in JavaFX SwingNode

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -m 'Add feature'`
4. Push: `git push origin feature/your-feature`
5. Open Pull Request

**Guidelines**: Add JavaDoc comments, follow Java conventions, test thoroughly.

## 📄 License

MIT License - see [LICENSE](LICENSE) file

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| App won't start | Ensure Java 17+ installed: `java -version` |
| Editor not showing | Wait for SwingNode to initialize |
| Simulation fails | Check you're not in reorder mode, verify text exists |
| Slow performance | Close other apps, increase typing speed |

## 📚 Resources

- [JavaFX Docs](https://openjfx.io/)
- [RSyntaxTextArea](https://bobbylight.github.io/RSyntaxTextArea/)
- [Stack Overflow JavaFX](https://stackoverflow.com/questions/tagged/javafx)

## 💬 Support

- [Open Issue](https://github.com/code-flu/typewriter/issues)
- Check existing issues first
- Provide Java version and OS details

## 👨‍💻 Author

**CodeFlu** - Created with ❤️

