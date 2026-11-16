# Manutenção e releases

## Alterar uma função

- Tema: `modules/theme/src/com/salutem/theme/Startup.java` e `modules/theme/resources/css/`.
- Idioma: `modules/language/src/com/salutem/l10n/Startup.java` e `translations.properties` em seus recursos.
- Ordenação: `modules/connection-order/src/com/salutem/order/`.
- Mensagens dos módulos: `translations/fragments/`; os textos em inglês estão em `translations/reference/`.
- Instalação: `installer/Instalar.ps1`.

Preserve os parâmetros `{0}`, `%s`, URLs e identificadores técnicos. Não traduza valores de campos editáveis, nomes de documentos, expressões ou dados do modelo.

O tema possui tratamentos de componentes internos do SWT, GEF, JFace e Nebula. Ao mudar a plataforma, revise esses tratamentos. Os testes de componentes não substituem a conferência visual no aplicativo completo.

## Compilar

Edite `versions.json`: aumente a versão de cada componente alterado e a versão do pacote. Atualize os manifests de origem correspondentes para manter a documentação dos recursos consistente.

```powershell
pwsh ./scripts/Build.ps1 -StudioPath 'C:\Program Files\Jaspersoft\Jaspersoft Studio-6.20.6'
```

O build recompila os três complementos, reempacota os 24 fragmentos e atualiza `artifacts/index.json`. Revise e faça commit dos fontes, JARs e índice juntos. Não atualize o índice manualmente para encobrir binários antigos.

Na primeira versão, `artifacts/plugins/` contém apenas as versões atuais. Depois de aumentar versões, remova do controle de versão os JARs substituídos antes de publicar; mantenha cópias de releases anteriores fora da árvore atual.

## Testar

```powershell
node ./scripts/Verify.mjs
javac -encoding UTF-8 -d build/test-classes modules/connection-order/src/com/salutem/order/NumericOrder.java tests/NumericOrderTest.java
java -cp build/test-classes NumericOrderTest
```

`Verify.mjs` confere hashes, correspondência entre fontes e binários, todas as chaves de tradução e os parâmetros. Há testes de componentes SWT/GEF em `tests/`; eles exigem as bibliotecas e DLLs do Studio 6.20.6 em Windows. `CheckPalette` usa o `PaletteViewer` real com preferências/plataforma simuladas e testa conteúdo, foco e pintura. Não é um teste do workbench completo.

Antes de publicar, abra o Studio e confira ao menos propriedades, consulta, preview, paleta, modelos, publicação, cancelamento do progresso e troca de idioma. Salve e feche os relatórios antes de trocar JARs.

## Publicar uma release

1. Atualize as versões e compile.
2. Execute as verificações e a conferência visual.
3. Crie `docs/releases/vX.Y.Z.md` com mudanças e limitações.
4. Faça commit e push para `main`.
5. Crie e envie uma tag correspondente a `packageVersion`:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

O workflow `Release` valida os artefatos, executa o teste numérico, gera o instalador e cria uma GitHub Release com o ZIP e seu SHA256. Alternativamente, execute `Release` pela aba Actions informando a versão. O workflow usa o `GITHUB_TOKEN` temporário do próprio GitHub com permissão de conteúdo; não exige PAT salvo no projeto.

Releases existentes não são sobrescritas automaticamente. Corrija a versão, faça novo build e publique uma nova tag.
