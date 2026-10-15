# BeYou — Floating Camera & Avatar Widget para Tablets Android

Aplicação nativa Android em Kotlin desenvolvida com foco em tablets rodando **Android 13+ (API 33+)**, implementando o design **BeYou Neon Dashboard**: um painel de controlo moderno com estética dark néon e um **Widget Flutuante Circular** (Floating Window Overlay) de alto impacto visual que permanece visível diretamente no ecrã inicial do tablet e por cima de qualquer outra aplicação (navegação, reuniões, apresentações).

---

## 🎨 Dashboard de Definições BeYou Neon

O layout foi desenhado para espelhar a referência visual com máxima fidelidade estética:

1. **Barra Superior**:
   - Logótipo luminoso em anel néon transparente (`Neon_Avatar_Camera_Portal-removebg-preview.png`) e identidade de marca **BeYou** (*Avatar e câmara flutuantes*).
   - Botão `[ 🎨 Tema ]` para seleção dinâmica entre temas (Néon Ciano, Néon Violeta, Néon Matrix e Modo Claro).
   - Botão pill de atalho `[ ⚙ Configurações ]` para as definições da aplicação no sistema.

2. **Card Permissões**:
   - Badge com ícone de escudo e diagnóstico de requisitos de sistema.
   - Linhas individuais para **Sobreposição** e **Câmara** com indicadores visuais de estado em tempo real (`● Concedida` / `● Pendente`) e botões `[ ⚙ Gerir ]`.

3. **Card Avatar**:
   - Badge de perfil e miniatura circular com efeito glow néon.
   - Ação primária `[ 🖼 Escolher fotografia ]` com gradiente ciano luminoso para upload de fotos personalizadas.
   - Ação secundária `[ 🔄 Restaurar padrão ]` para restaurar o avatar predefinido.

4. **Card Widget Flutuante**:
   - Badge de janelas em sobreposição e miniatura com o seletor circular de câmara.
   - Controlo de **Modo inicial**: seletor segmentado com alternância tátil em tempo real entre `[ 👤 Avatar ]` e `[ 📷 Câmara ]`.
   - Controlo de **Tamanho**: slider SeekBar e etiquetas táteis (*Pequeno*, *Médio*, *Grande*) com redimensionamento instantâneo do widget no ecrã.

5. **Barra Inferior de Ação**:
   - Botão pill primário com gradiente néon adaptativo `[ ▶ Iniciar widget ]`.
   - Botão pill de paragem com contorno carmesim `[ ■ Parar ]`.

---

## 🏛️ Arquitetura e Estrutura Modular

- **[MainActivity.kt](file:///Users/fetiwaetika/MobileProjects/floating-widget-tablet/app/src/main/java/com/antigravity/floatingwidget/MainActivity.kt)**: Gestão de eventos do dashboard, coordenação de temas e controlo do ciclo de vida.
- **[ThemeManager.kt](file:///Users/fetiwaetika/MobileProjects/floating-widget-tablet/app/src/main/java/com/antigravity/floatingwidget/theme/ThemeManager.kt)**: Gestor de paletas de cor, auréolas néon e persistência de temas.
- **[WidgetSettingsManager.kt](file:///Users/fetiwaetika/MobileProjects/floating-widget-tablet/app/src/main/java/com/antigravity/floatingwidget/settings/WidgetSettingsManager.kt)**: Controlo interativo de modo e tamanho do widget flutuante com comunicação IPC/Intent.
- **[card_permissions.xml](file:///Users/fetiwaetika/MobileProjects/floating-widget-tablet/app/src/main/res/layout/card_permissions.xml)**: Módulo isolado para verificação e gestão de permissões.
- **[card_avatar_settings.xml](file:///Users/fetiwaetika/MobileProjects/floating-widget-tablet/app/src/main/res/layout/card_avatar_settings.xml)**: Módulo de personalização do avatar do utilizador.
- **[card_widget_settings.xml](file:///Users/fetiwaetika/MobileProjects/floating-widget-tablet/app/src/main/res/layout/card_widget_settings.xml)**: Módulo de preferências do widget (modo inicial e tamanho).
- **[activity_main.xml](file:///Users/fetiwaetika/MobileProjects/floating-widget-tablet/app/src/main/res/layout/activity_main.xml)**: Layout principal agregador com botões de cabeçalho.
- **[AvatarManager.kt](file:///Users/fetiwaetika/MobileProjects/floating-widget-tablet/app/src/main/java/com/antigravity/floatingwidget/avatar/AvatarManager.kt)**: Gestor de persistência e carregamento de imagens no armazenamento interno.
- **[FloatingWidgetService.kt](file:///Users/fetiwaetika/MobileProjects/floating-widget-tablet/app/src/main/java/com/antigravity/floatingwidget/service/FloatingWidgetService.kt)**: Foreground Service com `WindowManager` para sobreposição contínua.

---

## 🛠️ Como Compilar e Executar

### 1. Compilação do APK
```bash
./gradlew assembleDebug
```
O APK gerado estará em: `app/build/outputs/apk/debug/app-debug.apk`.

### 2. Instalação e Execução via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.antigravity.floatingwidget/.MainActivity
```

---

## 🧪 Testes Unitários
```bash
./gradlew test
```
