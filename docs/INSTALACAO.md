# Instalação independente

Use o ZIP do componente desejado ou o conjunto com três pastas. Cada pasta tem seu próprio Instalar.bat; não existe mais instalação conjunta obrigatória.

- Temas: instala somente o complemento de cores. Escolha Noite, Grafite, Floresta, Ameixa, GitHub, Ônix ou Meia-Noite em Preferências > Gerais > Aparência. O inicializador não define idioma.
- Traducao-ptBR: instala somente os complementos de idioma. Selecione Português (Brasil) no menu Language e reinicie. Seu Abrir JS.bat inicia em pt_BR; o executável normal permite respeitar outro idioma salvo.
- Ordenacao-de-conexoes: instala somente a ordenação numérica.

Feche o Studio antes da instalação. O instalador solicita administrador, detecta ou pede a pasta do produto e valida Java 17+, Studio Community 6.20.6 e hashes. Instala em dropins/eclipse/plugins sem substituir os JARs originais. Componentes já instalados permanecem. Os complementos não devem ser usados diretamente no Studio 6.16.

Para desinstalar, feche o Studio e remova apenas os JARs listados em pacote/arquivos.json do componente correspondente, da pasta dropins/eclipse/plugins. Preserve todos os demais arquivos. Reinicie com -clean. O pacote anterior deve ser guardado para reversão.
Ao atualizar, o instalador guarda as versões anteriores dos complementos próprios em backups dentro da pasta do componente e retira essas versões da pasta de plugins. Se a cópia falhar, restaura os complementos anteriores. Módulos do produto e outros componentes são preservados.
