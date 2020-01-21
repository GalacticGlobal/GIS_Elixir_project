defmodule Hello1Web.SessionController do
  use Hello1Web, :controller

  alias Hello1.Accounts.Auth
  alias Hello1.Repo

  def new(conn, _params) do
    render(conn, "new.html",pagename: "Sign In Here")
  end

  def show(conn, _params) do
    conn = put_session(conn, :message, "new stuff we just set in the session")
    username = get_session(conn, :current_user_name)
    u_password = get_session(conn, :current_user_password)
    user_ssn = get_session(conn, :current_user_ssn)

    render(conn, "show.html" ,loggedInUser: username,userpass: u_password,u_ssn: user_ssn,pagename: "Welcome !!!.")
  end

   def registered(conn, _params) do
    user_name = get_session(conn, :new_user_name)
    user_pass = get_session(conn, :new_user_password)
    userssn = get_session(conn, :new_user_ssn)
    render(conn, "registered.html" ,newuser_name: user_name,user_pass: user_pass,ssn: userssn,pagename: "Registered successfully.")
  end

  @spec create(Plug.Conn.t(), map()) :: Plug.Conn.t()
  def create(conn, %{"session" => auth_params}) do
    case Auth.login(auth_params, Repo) do
    {:ok, user} ->
      conn
      |> put_session(:current_user_ssn, user.id)
      |> put_session(:current_user_name, user.username)
      |> put_session(:current_user_password, auth_params["password"])
      |> put_flash(:info, "Signed in successfully.")
      |> redirect(to: Routes.session_path(conn, :show))
    :error ->
      conn
      |> put_flash(:error, "There was a problem with your username/password")
      |> render("new.html")
    end
  end

  def delete(conn, _params) do
    conn
    |> delete_session(:current_user_ssn)
    |> delete_session(:current_user_name)
    |> delete_session(:current_user_password)
    |> put_flash(:info, "Signed out successfully.")
    |> redirect(to: Routes.session_path(conn, :new))
  end
end
