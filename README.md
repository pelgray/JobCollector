# JobCollector

Это сервис, позволяющий собирать интересные вакансии с hh.ru в одну таблицу, общаясь с Telegram-ботом.

Проведя в поиске некоторое время и сохранив предложения через этот сервис, Вы можете открыть получившуюся таблицу и
сравнить вакансии по общим параметрам, а также продолжать заполнять таблицу своими заметками.
Пример таблицы [тут](https://docs.google.com/spreadsheets/d/17NCV2zRHT08bpLNFjAUctYBJAFIuflN7AMVS8e7orS8)

## Как это работает?

Вот стандартный алгоритм использования:
1. Вы просматриваете вакансии и натыкаетесь на интересное предложение.
0. В зависимости от используемого устройства:
    * _Браузер_.  
    Копируете ссылку страницы с вакансией и отправляете боту.
    * _Мобильное приложение hh.ru_.  
    Отправляете ссылку на вакансию через "Поделиться" в чат с ботом.
0. Бот отвечает, смог ли он добавить вакансию.
0. Возвращаетесь к пункту 1.

## Как запустить?

Для того, чтобы запустить сервис, Вам потребуются:
1. Telegram бот.   
О том, как его создать, написано [здесь](https://core.telegram.org/bots#creating-a-new-bot).
Вам необходимо запомнить токен и имя (`username`) Вашего бота.
0. Google аккаунт.   
0. Пустая Google таблица.   
Сервис должен знать `spreadsheetId` этой таблицы. А чтобы его получить, посмотрите
 [эту ссылку](https://developers.google.com/sheets/api/guides/concepts#spreadsheet_id).
0. Проект на [Google API Console](https://console.developers.google.com/).   
Данный сервис использует спецификацию OAuth 2.0 для авторизации приложения и доступа через него к защищенным ресурсам
на стороне серверов Google (то есть, к Вашей пока еще пустой таблице). Чтобы иметь возможность взаимодействовать с
сервисами Google через API, необходимо иметь файл `client_secrets.json`, способ получения которого описан **тут... будет когда-нибудь**.

Когда все ингредиенты получены, можно создавать файл `config.properties` по следующему шаблону:
```
tgBot.Name=
tgBot.Token=
gSheets.SpreadsheetId=
```
где после `=` надо написать информацию о Telegram-боте и созданной таблице без кавычек и пробелов.

Для удобства файлы `config.properties` и `client_secrets.json` можно положить рядом со скаченной версией
`JobCollector.jar`, а затем производить запуск по команде:
```
java -jar JobCollector.jar
```
Но также предоставляется возможность указать путь до каталога с любым из настроечных файлов через параметры `app.props`
для `config.properties` и `app.secrets` для `client_secrets.json`. Тогда запуск приложения будет следующим: 
```
java -jar -Dapp.props="path\to\file" -Dapp.secrets="path\to\file" JobCollector.jar
```
