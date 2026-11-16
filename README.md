# Jaspersoft Studio Customizations

Personalizações para o **Jaspersoft Studio Community 6.20.6 no Windows**, com Java 17 ou superior.

| Componente | Versão | Função |
|---|---|---|
| Salutem Noite | 1.7.5 | Tema azul escuro, contraste, paleta, galerias, assistentes e progresso |
| Português (Brasil) | 2.0.2 | Tradução da interface, rótulos complementares e bandeira brasileira |
| Ordenação de conexões | 1.0.0 | Ordenação por código numérico em consulta e preview |
| Fragmentos de tradução | 2.0.0 | 49 recursos / 6.496 entradas em 24 módulos |

## Instalação

Baixe o ZIP em [Releases](https://github.com/Luckmaiqui/jaspersoft-studio-customizations/releases), extraia e abra a pasta **Tradução e Tema JS**.

1. Salve os relatórios e feche o Studio.
2. Execute **Instalar.bat** e aceite a solicitação de administrador do Windows.
3. Se necessário, selecione a pasta que contém `plugins` e `Jaspersoft Studio.ini`.
4. Abra pelo arquivo **Abrir JS.bat**.
5. Em um workspace existente, selecione **Janela → Preferências → Gerais → Aparência → Salutem Noite**.

O inicializador fornece `-nl pt_BR`; para respeitar outro idioma salvo, use o executável normal do Studio. A tradução complementar só atua no idioma português. A ordenação é independente do idioma e do tema.

Leia [Instalação e compatibilidade](docs/INSTALACAO.md).

## Recursos

- Fundos azul-escuros, texto legível, divisórias escuras e seleção destacada.
- Tratamento específico para propriedades, paleta GEF, galeria Nebula, editor, preview, assistentes e progresso.
- Tradução dos recursos próprios do Jasper/GEF, mais os pacotes Babel pt_BR da base Eclipse.
- Ajuste de rótulos fixos sem editar consultas, expressões ou conteúdo dos relatórios.
- Conexões ordenadas como `(001)`, `(002)`, `(014)`, `(103)`; cópias ficam junto do respectivo código.
- Instalador com detecção de versão, validação SHA256 e bloqueio de arquivos existentes divergentes.

As instalações são feitas em `dropins/eclipse/plugins`; os JARs originais do aplicativo não são substituídos. A antiga tentativa de personalizar o splash não faz parte do pacote: ela foi revertida.

## Compatibilidade

Este pacote é específico da composição do Studio **6.20.6** usada no desenvolvimento. **Não é diretamente compatível com 6.16**: os complementos usam Java 17 e os fragmentos se vinculam às versões exatas dos módulos. Uma edição para outra versão exige recompilação, revisão dos recursos e testes naquela plataforma.

Não foi validado em Linux, macOS ou edição Professional. A aparência nativa pode variar entre versões do Windows. As traduções foram geradas em lote com revisão dos termos principais; correções editoriais são bem-vindas.

## Desenvolvimento

Requisitos: PowerShell 7, Node.js 20 ou superior, Java 17 e uma instalação local do Studio 6.20.6. O produto completo não é distribuído neste repositório.

```powershell
pwsh ./scripts/Build.ps1 -StudioPath 'C:\Program Files\Jaspersoft\Jaspersoft Studio-6.20.6'
```

O build usa o compilador ECJ e as dependências da instalação informada, gera os JARs das personalizações, atualiza o índice de hashes e cria o ZIP em `dist/`.

Para empacotar os binários já registrados, sem recompilar:

```powershell
pwsh ./scripts/Build.ps1 -PackageOnly
```

Se os fontes não corresponderem ao índice dos binários, essa operação falha. Consulte [Manutenção e releases](docs/DESENVOLVIMENTO.md).

## Estrutura

```text
modules/             Fontes e recursos de tema, idioma e ordenação
translations/        Recursos pt_BR e referências em inglês
installer/           Instalador Windows e inicializador portátil
vendor/eclipse-babel Pacotes de idioma de terceiros
artifacts/           JARs compilados e índice de integridade
scripts/             Build e verificação
tests/               Testes de regressão Java/SWT/GEF
.github/workflows/   Validação e publicação de releases
```

## Autoria e componentes de terceiros

Personalizações preparadas para Luckas / Salutem. Este projeto não é um produto oficial da Jaspersoft ou do Eclipse. Veja [avisos de terceiros](THIRD_PARTY_NOTICES.md). A licença dos arquivos próprios ainda será definida pelo titular; o repositório não declara uma licença aberta para esses arquivos.
