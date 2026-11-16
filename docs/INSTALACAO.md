# Instalação

O pacote contém o tema, a tradução e a ordenação de conexões para Jaspersoft Studio Community 6.20.6, em Windows com Java 17 ou superior.

1. Extraia o ZIP da release, mantendo todos os arquivos juntos.
2. Salve os relatórios e feche o Studio.
3. Execute `Instalar.bat`. O Windows solicitará elevação de administrador.
4. O instalador procura o aplicativo em Program Files, Program Files (x86) e AppData/Local/Programs. Se houver mais de uma instalação ou nenhuma for localizada, escolha a pasta do Studio.
5. Execute `Abrir JS.bat`.
6. Em um workspace já existente, selecione o tema **Salutem Noite** em **Janela → Preferências → Gerais → Aparência** e reinicie quando solicitado.

`Abrir JS.bat` passa `-clean`, `-nl pt_BR` e o arquivo `padroes.ini`. Novos workspaces recebem o tema como padrão; preferências existentes do workspace podem exigir a escolha manual acima. O INI original do aplicativo não é alterado. Para usar outro idioma salvo, abra pelo executável normal.

## Validação sem instalar

```powershell
powershell -ExecutionPolicy Bypass -File .\Instalar.ps1 -StudioPath 'D:\Apps\Jaspersoft Studio' -ValidateOnly
```

São conferidos a versão do Studio, o Java, os hosts dos fragmentos e os hashes dos JARs. Fragmentos opcionais cujos módulos não existem são ignorados; versões de host incompatíveis são recusadas.

O instalador recusa um arquivo de mesmo nome cujo conteúdo seja diferente. Ele pode ser executado novamente para conferir uma instalação idêntica.

## Atualizar

Extraia a nova release em outra pasta e execute seu `Instalar.bat` com o Studio fechado. O Eclipse seleciona a versão mais recente dos complementos singleton. Mantenha o pacote anterior caso precise voltar a uma versão conhecida.

## Compatibilidade

O Studio 6.16 utiliza outra plataforma Eclipse e foi distribuído com Java 11. Este pacote não deve ser instalado nessa versão. Alterar somente os números dos manifests ou trocar o Java não garante compatibilidade.

Referência oficial: [compilação do Jaspersoft Studio 6.16](https://community.jaspersoft.com/knowledgebase/faq/contributing-tibco-jaspersoft-studio-and-building-sources/).

O pacote não altera os dados das conexões, os nomes dos arquivos nem o conteúdo dos relatórios. A ordenação é aplicada aos armazenamentos de adaptadores em memória, preservando os mesmos objetos e identificadores.
