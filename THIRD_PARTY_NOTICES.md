# Componentes de terceiros

Este repositório distribui personalizações para um aplicativo que deve estar instalado separadamente.

## Eclipse Babel

Os JARs em `vendor/eclipse-babel/` são fragmentos pt_BR do Eclipse Babel 4.26.0, obtidos do pacote de idioma oficial de 2023-02 e filtrados pelos módulos usados nesta instalação do Studio. Permanecem inalterados, com seus manifests, avisos e licenças originais. Alguns podem ser ignorados pelo instalador quando o módulo correspondente estiver ausente.

Projeto: https://babel.eclipse.org/ — consulte também os arquivos de licença dentro de cada JAR.

## Jaspersoft Studio e Eclipse/GEF

Os recursos de referência em inglês foram extraídos dos módulos instalados do Studio 6.20.6 e GEF 3.14.0, para manutenção das traduções. Os fragmentos pt_BR derivam desses recursos. Os JARs executáveis originais do Jaspersoft Studio, SWT, JFace, GEF e Nebula não são distribuídos aqui; o build usa uma instalação fornecida pelo desenvolvedor.

A bandeira em `modules/language/resources/icons/br.png` foi reutilizada do recurso `icons/flags/br.png` da instalação do Studio. As marcas Jaspersoft, JasperReports e Eclipse pertencem a seus titulares. Este projeto é independente desses projetos.

## Arquivos próprios

A licença de redistribuição dos arquivos próprios ainda não foi definida pelo titular. Não se atribui uma licença única aos componentes de terceiros, nem se substituem seus avisos originais.
