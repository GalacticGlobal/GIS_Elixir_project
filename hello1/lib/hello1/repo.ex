defmodule Hello1.Repo do
  use Ecto.Repo,
    otp_app: :hello1,
    adapter: Ecto.Adapters.Postgres
end
