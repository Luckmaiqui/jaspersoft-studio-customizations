# Pacote de Atualizações do JasperStudio

Complementos independentes para **Jaspersoft Studio Community 6.20.6 no Windows, Java 17+**.

## Escolha o que instalar

Baixe em [Releases](https://github.com/Luckmaiqui/pacote-de-atualizacoes-do-jasperstudio/releases):

| ZIP | Pasta | Conteúdo |
|---|---|---|
| JS-Temas-v2.1.1.zip | Temas | Salutem Noite (azul), Grafite, Floresta (verde) e Ameixa (roxo) |
| JS-Traducao-ptBR-v2.1.1.zip | Traducao-ptBR | Tradução para Português (Brasil) e bandeira |
| JS-Ordenacao-v2.1.1.zip | Ordenacao-de-conexoes | Pesquisa por código/nome no preview e dataset, mais ordenação numérica |
| JS-Pacotes-Separados-v2.1.1.zip | Pacote de Atualizações do JasperStudio | As três pastas acima, com instaladores separados |

Cada pasta tem **Instalar.bat** e **Abrir JS.bat** próprios. Você pode instalar apenas uma, duas ou todas. O pacote de temas não instala tradução nem muda o idioma. O pacote de tradução não instala nem seleciona tema. A ordenação também é independente.

1. Extraia o ZIP, salve seus relatórios e feche o Studio.
2. Execute o Instalar.bat da pasta desejada e aceite a elevação do Windows.
3. Informe a instalação do Studio se solicitado.
4. Abra pelo Abrir JS.bat da pasta escolhida.
5. Para trocar cores: **Janela > Preferências > Gerais > Aparência > Tema**.
6. Para traduzir: **Janela > Language > Português (Brasil)** e reinicie; o inicializador da tradução também fornece `-nl pt_BR`.

O inicializador de temas respeita o idioma salvo; o padrão de tema para novos workspaces é Salutem Noite. Instalar um componente não remove componentes já instalados. A tradução complementar atua quando o idioma ativo é português.

## Recursos e compatibilidade

Os quatro temas compartilham os ajustes de propriedades, paleta, menus, galeria, editor, preview e assistentes. O papel do relatório e seu conteúdo não são modificados. As cores de texto principais, campos e seleção são verificadas com contraste mínimo 4,5:1.

A tradução reúne 49 recursos / 6.496 entradas em 24 fragmentos, pacotes Eclipse Babel e rótulos complementares. A ordenação agrupa códigos como `(001)`, `(002)`, `(014)`, `(103)`.

Os complementos são copiados para `dropins/eclipse/plugins`, com validação SHA256 e bloqueio de arquivos existentes divergentes. Os JARs originais do produto não são substituídos.

**Não compatível diretamente com 6.16**: usa Java 17 e fragmentos vinculados às versões exatas dos módulos. Outras versões, Linux, macOS e Professional não foram validados. Traduções em lote podem exigir revisão editorial.

## Desenvolvimento

Requisitos: PowerShell 7, Node.js 20+, Java 17 e Studio 6.20.6 local para recompilar.

```powershell
pwsh ./scripts/Build.ps1 -StudioPath 'C:\Program Files\Jaspersoft\Jaspersoft Studio-6.20.6'
# Apenas empacotar artefatos já verificados:
pwsh ./scripts/Build.ps1 -PackageOnly
```

As paletas ficam em `modules/theme/resources/palettes.properties`. `scripts/GenerateThemes.mjs` gera CSS e registros de tema. Ao alterar código de um módulo, aumente sua versão em `versions.json`; os outros componentes mantêm seus binários. O build preserva JARs sem mudança de conteúdo e rejeita mudança sem aumento de versão.

A saída contém quatro ZIPs e SHA256SUMS.txt em `dist/`. O workflow Release publica todos esses arquivos, a partir de uma tag ou execução manual com a versão de `versions.json`.

Veja [instalação](docs/INSTALACAO.md), [desenvolvimento](docs/DESENVOLVIMENTO.md) e [avisos de terceiros](THIRD_PARTY_NOTICES.md).

Personalizações preparadas para Luckas / Salutem. Não é produto oficial Jaspersoft/Eclipse. A licença dos arquivos próprios ainda deve ser definida pelo titular; o repositório não declara licença aberta para eles.
## Pesquisa de conexões

Instale o pacote Ordenacao-de-conexoes atualizado. Ao abrir a lista de adaptadores no preview ou no dataset, digite o código ou parte do nome. A pesquisa ignora maiúsculas e acentos e aceita vários termos. Use as setas e Enter, clique em uma opção ou pressione Esc para cancelar. Não altera credenciais, consultas nem executa pesquisa no servidor; filtra somente os rótulos das conexões disponíveis.
