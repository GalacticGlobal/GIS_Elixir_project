defmodule Hello1Web.UserController do
  use Hello1Web, :controller

  alias Hello1.Accounts
  alias Hello1.Accounts.User
  
  def new(conn, _params) do
    changeset = Accounts.change_user(%User{})
    user_ssn_ip = conn.remote_ip |> Tuple.to_list |> Enum.join(".")
    render(conn, "new.html", changeset: changeset,pagename: "Sign Up Here",ssnip: user_ssn_ip)
  end

  def create(conn, %{"user" => user_params}) do
    case Accounts.create_user(user_params) do
      {:ok, user} ->
        conn
        |> put_session(:new_user_name, user.username)
        |> put_session(:new_user_password, user_params["password"])
        |> put_session(:new_user_ssn, user.inserted_at)
        |> put_flash(:info, "Signed up successfully.")
        |> redirect(to: Routes.session_path(conn, :registered))
      {:error, %Ecto.Changeset{} = changeset} ->
        render(conn, "new.html", changeset: changeset)
      end
  end

end
