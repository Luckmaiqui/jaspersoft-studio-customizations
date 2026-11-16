# Versão 2.0.0

- Instaladores independentes para temas, tradução pt-BR e ordenação de conexões.
- Quatro temas: Salutem Noite, Grafite, Floresta e Ameixa.
- O inicializador dos temas respeita o idioma escolhido, sem forçar português.
- Tradução e ordenação mantêm suas versões e seus binários anteriores.
- Quatro downloads: temas, tradução, ordenação e conjunto com pastas separadas.
- Build preserva JARs inalterados e exige aumento de versão para código alterado.

Compatível com Jaspersoft Studio Community 6.20.6, Windows, Java 17+. Não compatível diretamente com 6.16.

Validação: integridade dos 153 JARs, 6.496 chaves de tradução, contraste das quatro paletas, alternância das cores em Gallery/ProgressMonitorPart reais e validação dos instaladores pelo Windows PowerShell 5.1. A aplicação completa deve ser reiniciada para carregar o novo complemento.
# Histórico

## 1.0.0 — 2026-11-16

Primeiro pacote versionado do projeto, reunindo:

- Tema Salutem Noite 1.7.5.
- Português (Brasil) 2.0.2, bandeira e 24 fragmentos de tradução.
- Ordenação numérica de conexões 1.0.0.
- Instalador portátil para Windows/Studio 6.20.6.
- Fontes, verificações e workflows de validação/release.

Correções mais recentes: remover `@Ctrl+S` do botão Salvar; traduzir a pergunta de salvamento preservando o nome do arquivo; escurecer o progresso da publicação; definir uma cor nativa única para rótulos desabilitados.

A antiga tentativa de splash personalizado foi revertida e não é distribuída.
