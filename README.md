# WordComposerMicroservice

> [!CAUTION]
> Это тестовый проект для презентации. Его запуск не подразумевается из-за комплексности, хотя это и возможно при должном желании 😃

> [!NOTE]  
> Данное приложение помогает обрабатывать **англоязычные** слова, которые в последствии получают **перевод, транскрипцию, значение, а также изображение для создания карточки слова**.

> [!TIP]
> Требуется всё необходимое:
> * GITHUB CLOUD CONFIG SETTINGS -> https://github.com/hannahmontana-554/word-composer-microservices/tree/master/spring_cloud_config_settings-master
> * API KEYS_SECRETS(https://dictionaryapi.dev/, https://unsplash.com/, https://imgbb.com/)
> * S3 Container
> * Self-hosted LibreTranslate in Docker
> * Postgresql DB
> * Elastic-Stack for logging(Optional) -> https://github.com/hannahmontana-554/elk-stack
> * minikube -> strimzi config with 3 kafka topics: initial-topic, image-handler-topic, card-representor-topic
