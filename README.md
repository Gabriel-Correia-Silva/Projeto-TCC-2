# RingCare (Projeto-TCC-2)

RingCare é um aplicativo Android de monitoramento de saúde projetado para ajudar cuidadores e supervisores a acompanhar o bem-estar de seus entes queridos. Utilizando dados de um anel inteligente (Colmi Ring) e do Health Connect do Android, o aplicativo oferece uma visão completa dos sinais vitais, atividade diária e detecção de quedas, promovendo um cuidado proativo e conectado.

## ✨ Principais Funcionalidades

- **Dashboard Duplo:** Interfaces distintas para o usuário "Supervisionado" (paciente) e o "Supervisor" (cuidador), garantindo que cada um tenha acesso às informações relevantes.
- **Monitoramento de Saúde Abrangente:**
    - **Frequência Cardíaca:** Acompanhamento em tempo real e histórico diário.
    - **Passos e Atividade:** Contagem de passos, distância percorrida e metas diárias.
    - **Qualidade do Sono:** Análise detalhada das fases do sono (Leve, Profundo, REM) e duração total.
    - **Saturação de Oxigênio (SpO2):** Leituras da oxigenação sanguínea.
    - **Calorias:** Estimativa de calorias queimadas.
- **Detecção de Quedas:** Utiliza o acelerômetro do anel para detectar possíveis quedas e acionar um alerta com contagem regressiva para confirmação, notificando contatos de emergência se necessário.
- **Comunicação Integrada:**
    - **Feedback Direto:** Supervisores podem enviar mensagens e recomendações diretamente pelo aplicativo.
    - **Notificações:** O usuário supervisionado recebe alertas e feedbacks em uma tela dedicada.
- **Contatos de Emergência:** O usuário pode configurar contatos de emergência que podem ser acionados rapidamente através de um botão flutuante ou automaticamente após a detecção de uma queda.
- **Sincronização de Dados:** Os dados de saúde são sincronizados com o Firebase Firestore para permitir o monitoramento remoto pelo supervisor.
- **Integração com Sensores:**
    - **Anel Colmi:** Conexão via Bluetooth LE para coletar dados diretamente do anel.
    - **Health Connect:** Capacidade de ler dados de saúde consolidados do sistema Android.

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **UI:** Jetpack Compose
- **Arquitetura:** MVVM (Model-View-ViewModel)
- **Injeção de Dependência:** Hilt
- **Banco de Dados Local:** Room
- **Backend & Sincronização:** Firebase (Authentication, Firestore)
- **Comunicação de Rede:** Retrofit
- **Gerenciamento de Tarefas Assíncronas:** Kotlin Coroutines & Flow
- **Dados de Saúde:** Health Connect SDK
- **Conectividade:** Bluetooth Low Energy (BLE)

## 🏛️ Arquitetura

O projeto segue a arquitetura **MVVM (Model-View-ViewModel)**, que promove uma separação clara de responsabilidades:

- **View (Screens):** Camada de UI, construída com Jetpack Compose. É responsável por exibir os dados e notificar o ViewModel sobre as interações do usuário.
- **ViewModel:** Atua como uma ponte entre a View e o Model. Contém a lógica de apresentação e gerencia o estado da UI, expondo-o através de `StateFlow` ou `LiveData`.
- **Model (Repositories & Data Sources):** Camada de dados responsável por buscar e gerenciar os dados, seja de fontes remotas (Firebase, API), locais (Room, DataStore) ou de sensores (BLE, Health Connect).

A **Injeção de Dependência** com Hilt é usada para desacoplar as classes e facilitar a testabilidade e a manutenção do código.

## 🚀 Como Executar o Projeto

1.  **Clone o Repositório:**
    ```bash
    git clone [https://github.com/gabriel-correia-silva/Projeto-TCC-2.git](https://github.com/gabriel-correia-silva/Projeto-TCC-2.git)
    ```
2.  **Abra no Android Studio:**
    - Abra o Android Studio (versão Hedgehog ou mais recente).
    - Selecione "Open an Existing Project" e navegue até a pasta do projeto clonado.
3.  **Configuração do Firebase:**
    - O projeto já contém um arquivo `google-services.json`. Para usar seu próprio backend Firebase, crie um novo projeto no [Firebase Console](https://console.firebase.google.com/).
    - Adicione um aplicativo Android ao seu projeto Firebase com o nome do pacote `com.example.projeto_ttc2`.
    - Baixe o novo arquivo `google-services.json` e substitua o existente na pasta `app/`.
    - Ative o **Firebase Authentication** (com o provedor Google) e o **Cloud Firestore**.
4.  **Sincronize e Compile:**
    - O Android Studio irá sincronizar o projeto com os arquivos Gradle. Isso pode levar alguns minutos.
    - Após a sincronização, compile e execute o aplicativo em um emulador ou dispositivo físico com Android.

**Permissões:** O aplicativo solicitará diversas permissões em tempo de execução (Localização, Bluetooth, Notificações, Atividade Física). É necessário concedê-las para que todas as funcionalidades operem corretamente.
