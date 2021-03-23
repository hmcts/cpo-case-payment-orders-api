# CPO fullstack docker :whale:

- [Prerequisites](#prerequisites)-
- [License](#license)

## Prerequisites

- [Docker](https://www.docker.com)
- psql (Tested with version 12.4)
- `ccd-docker` environment configured and running,
see [Run `ccd-docker` containers](#Run-ccd-docker-containers) for details

### Environment Variables
- Ensure the relevant environment variables in `cpo-docker/bin/env_variables-all.txt` are set by running

    ```bash
    cd cpo-docker/bin
    source env_variables_all.txt
  ```

### Run `ccd-docker` containers
- Install and run CCD stack as advised [here](https://github.com/hmcts/ccd-docker).

    ```bash
    ./ccd enable backend sidam sidam-local sidam-local-ccd
    ```

## CPO

Please run cpo docker as follows.
```
> cd cpo-docker
> docker-compose -f compose/cpo.yml up -d
```

### Compose branches

By default, tha CPO container will be running the `latest` tag, built from the `master` branch.  However, this behaviour can be changed by using the environment variable: `CASE_PAYMENT_ORDERS_API_TAG`.

#### Switch to a branch

To switch to a branch (e.g. `pr-126`): `set` the environment variable and update the containers:

```bash
> export CASE_PAYMENT_ORDERS_API_TAG=<branch>
> docker-compose -f compose/cpo.yml up -d
```

#### Revert to `master`

To revert to `master`: `unset` the environment variable and update the containers:

```bash
> unset CASE_PAYMENT_ORDERS_API_TAG
> docker-compose -f compose/cpo.yml up -d
```

## LICENSE

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
